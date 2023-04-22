/*
 * File: cheese.scala                                                          *
 * Created Date: 2023-02-26 09:45:59 am                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2023-04-12 11:43:36 am                                       *
 * Modified By: Mathieu Escouteloup                                            *
 * -----                                                                       *
 * License: See LICENSE.md                                                     *
 * Copyright (c) 2023 HerdWare                                                 *
 * -----                                                                       *
 * Description:                                                                *
 */


package herd.pltf.cheese

import chisel3._
import chisel3.util._

import herd.common.bus._
import herd.common.field._
import herd.common.mem.mb4s._
import herd.common.mem.axi4._
import herd.common.mem.ram._
import herd.core.aubrac._
import herd.core.aubrac.common._
import herd.core.salers.{Salers}
import herd.core.abondance.{Abondance}
import herd.io.periph.uart.{UartIO}
import herd.io.periph.ps2.{Ps2IO}
import herd.io.periph.spi.{SpiIO}
import herd.io.periph.i2c.{I2cIO}
import herd.io.pltf._


class Cheese (p: CheeseParams) extends Module {
  require ((p.nCore == 1), "Only one core is possible.")
  require ((p.nAubrac < 2),     "Only one Aubrac core is possible.")
  require ((p.nSalers < 2),  "Only one Salers core is possible.")
  require ((p.nAbondance < 2),  "Only one Abondance core is possible.")
  
  val io = IO(new Bundle {
    val b_gpio = Vec(p.nGpio32b, new BiDirectIO(UInt(32.W)))
    val b_spi_flash = if (p.useSpiFlash) Some(new SpiIO(1)) else None
    val b_ps2_kb = if (p.usePs2Keyboard) Some(new Ps2IO()) else None
    val b_uart = Vec(p.nUart, new UartIO())
    val b_spi = MixedVec(
      for (ps <- p.pIO.pSpi) yield {
        new SpiIO(ps.nSlave)
      }
    )
    val b_i2c = Vec(p.nI2c, new I2cIO())

    val o_dbg = if (p.debug) Some(Output(new CheeseDbgBus(p))) else None
    val o_etd = if (p.debug) Some(Output(Vec(p.nCommit, new EtdBus(p.nHart, p.nAddrBit, p.nInstrBit)))) else None
  })  
  
  val m_aubrac = for (pa <- p.pAubrac) yield {
    val m_aubrac = Module(new Aubrac(pa))
    m_aubrac
  } 
  val m_salers = for (ps <- p.pSalers) yield {
    val m_salers = Module(new Salers(ps))
    m_salers
  } 
  val m_abondance = for (pa <- p.pAbondance) yield {
    val m_abondance = Module(new Abondance(pa))
    m_abondance
  } 
  val m_llcross = Module(new Mb4sCrossbar(p.pLLCross))
  val m_boot = Module(new Mb4sDataRam(p.pBoot))
  val m_rom = if (p.useRom) Some(Module(new Mb4sDataRam(p.pRom))) else None 
  val m_ram = if (p.useRom) Some(Module(new Mb4sDataRam(p.pRam))) else None 
  val m_io = Module(new IOPltf(p.pIO))
  val m_pall = if (p.useField) Some(Module(new Part2Rsrc(1, p.nField, p.nPart, p.nPart))) else None

  // ******************************
  //              CORE
  // ******************************  
  // Interrupts
  if (p.useChamp) {
    for (tl <- 0 until p.nChampTrapLvl) {
      if (p.nAbondance > 0) {
        m_abondance(0).io.i_irq_lei.get(tl) := m_io.io.o_irq_lei.get(0)(tl)
        m_abondance(0).io.i_irq_lsi.get(tl) := m_io.io.o_irq_lsi.get(0)(tl)
      } else if (p.nSalers > 0) {
        m_salers(0).io.i_irq_lei.get(tl) := m_io.io.o_irq_lei.get(0)(tl)
        m_salers(0).io.i_irq_lsi.get(tl) := m_io.io.o_irq_lsi.get(0)(tl)
      } else {
        m_aubrac(0).io.i_irq_lei.get(tl) := m_io.io.o_irq_lei.get(0)(tl)
        m_aubrac(0).io.i_irq_lsi.get(tl) := m_io.io.o_irq_lsi.get(0)(tl)
      }
    }
  } else {
    if (p.nAbondance > 0) {
      m_abondance(0).io.i_irq_mei.get := m_io.io.o_irq_mei.get(0)
      m_abondance(0).io.i_irq_msi.get := m_io.io.o_irq_msi.get(0)
    } else if (p.nSalers > 0) {
      m_salers(0).io.i_irq_mei.get := m_io.io.o_irq_mei.get(0)
      m_salers(0).io.i_irq_msi.get := m_io.io.o_irq_msi.get(0)
    } else {
      m_aubrac(0).io.i_irq_mei.get := m_io.io.o_irq_mei.get(0)
      m_aubrac(0).io.i_irq_msi.get := m_io.io.o_irq_msi.get(0)
    }
  }

  // ******************************
  //          FIELD SELECT
  // ******************************  
  val m_slct = if (p.useField) Some(Module(new StaticSlct(p.nField, p.nPart, 1))) else None

  if (p.useField) {
    if (p.nAbondance > 0) {
      m_pall.get.io.b_part <> m_abondance(0).io.b_pall.get
    } else if (p.nSalers > 0) {
      m_pall.get.io.b_part <> m_salers(0).io.b_pall.get
    } else {
      m_pall.get.io.b_part <> m_aubrac(0).io.b_pall.get
    }
    for (f <- 0 until p.nField) {
      m_slct.get.io.i_weight(f) := m_pall.get.io.b_rsrc.weight(f)
    }
  }
  
  // ******************************
  //        BUS INTERCONNECT
  // ******************************  
  var mem: Int = 0
  
  if (p.useField) {
    if (p.nAbondance > 0) {
      m_llcross.io.b_field.get <> m_abondance(0).io.b_field.get
    } else if (p.nSalers > 0) {
      m_llcross.io.b_field.get <> m_salers(0).io.b_field.get
    } else {
      m_llcross.io.b_field.get <> m_aubrac(0).io.b_field.get
    }    
    m_llcross.io.i_slct_req.get := m_slct.get.io.o_slct
    m_llcross.io.i_slct_write.get := m_slct.get.io.o_slct
    m_llcross.io.i_slct_read.get := m_slct.get.io.o_slct
  }

  var v_mllcross: Int = 0
  for (a <- 0 until p.nAbondance) {
    if (p.pAbondance(a).useL2) {
      m_llcross.io.b_m(v_mllcross + 0) <> m_abondance(a).io.b_mem.get
      v_mllcross = v_mllcross + 1
    } else {
      if (p.pAbondance(a).useL1D) {
        m_llcross.io.b_m(v_mllcross + 0) <> m_abondance(a).io.b_dmem.get
        v_mllcross = v_mllcross + 1
      } else {
        m_llcross.io.b_m(v_mllcross + 0) <> m_abondance(a).io.b_d0mem.get
        m_llcross.io.b_m(v_mllcross + 1) <> m_abondance(a).io.b_d1mem.get
        v_mllcross = v_mllcross + 2
      }
      m_llcross.io.b_m(v_mllcross + 0) <> m_abondance(a).io.b_imem.get
      v_mllcross = v_mllcross + 1
    }
  }

  for (s <- 0 until p.nSalers) {
    if (p.pSalers(s).useL2) {
      m_llcross.io.b_m(v_mllcross + 0) <> m_salers(s).io.b_mem.get
      v_mllcross = v_mllcross + 1
    } else {
      m_llcross.io.b_m(v_mllcross + 0) <> m_salers(s).io.b_dmem.get
      m_llcross.io.b_m(v_mllcross + 1) <> m_salers(s).io.b_imem.get
      v_mllcross = v_mllcross + 2
    }
  }

  for (a <- 0 until p.nAubrac) {
    if (p.pAubrac(a).useL2) {
      m_llcross.io.b_m(v_mllcross + 0) <> m_aubrac(a).io.b_mem.get
      v_mllcross = v_mllcross + 1
    } else {
      m_llcross.io.b_m(v_mllcross + 0) <> m_aubrac(a).io.b_dmem.get
      m_llcross.io.b_m(v_mllcross + 1) <> m_aubrac(a).io.b_imem.get
      v_mllcross = v_mllcross + 2
    }      
  }

  // ******************************
  //         ROM, RAM & I/Os
  // ******************************
  // ------------------------------
  //              BOOT
  // ------------------------------
  if (p.useField) {
    if (p.nAbondance > 0) {
      m_boot.io.b_field.get <> m_abondance(0).io.b_field.get        
    } else if (p.nSalers > 0) {
      m_boot.io.b_field.get <> m_salers(0).io.b_field.get        
    } else {
      m_boot.io.b_field.get <> m_aubrac(0).io.b_field.get        
    }
    m_boot.io.i_slct.get := m_slct.get.io.o_slct
  }
  m_boot.io.b_port(0) <> m_llcross.io.b_s(mem)
  m_boot.io.b_port(1) <> m_llcross.io.b_s(mem + 1)
  mem = mem + 2  

  // ------------------------------
  //              ROM
  // ------------------------------
  if (p.useRom) {
    if (p.useField) {
      if (p.nAbondance > 0) {
        m_rom.get.io.b_field.get <> m_abondance(0).io.b_field.get        
      } else if (p.nSalers > 0) {
        m_rom.get.io.b_field.get <> m_salers(0).io.b_field.get        
      } else {
        m_rom.get.io.b_field.get <> m_aubrac(0).io.b_field.get        
      }
      m_rom.get.io.i_slct.get := m_slct.get.io.o_slct
    }
    m_rom.get.io.b_port(0) <> m_llcross.io.b_s(mem)
    m_rom.get.io.b_port(1) <> m_llcross.io.b_s(mem + 1)
    mem = mem + 2
  }

  // ------------------------------
  //              RAM
  // ------------------------------
  if (p.useRam) {
    if (p.useField) {
      if (p.nAbondance > 0) {
        m_ram.get.io.b_field.get <> m_abondance(0).io.b_field.get
      } else if (p.nSalers > 0) {
        m_ram.get.io.b_field.get <> m_salers(0).io.b_field.get
      } else {
        m_ram.get.io.b_field.get <> m_aubrac(0).io.b_field.get
      }      
      m_ram.get.io.i_slct.get := m_slct.get.io.o_slct
    }
    m_ram.get.io.b_port(0) <> m_llcross.io.b_s(mem)
    m_ram.get.io.b_port(1) <> m_llcross.io.b_s(mem + 1)
    mem = mem + 2
  }

  // ------------------------------
  //             I/Os
  // ------------------------------
  if (p.useField) {
    if (p.nAbondance > 0) {
      m_io.io.b_field.get <> m_abondance(0).io.b_field.get
    } else if (p.nSalers > 0) {
      m_io.io.b_field.get <> m_salers(0).io.b_field.get
    } else {
      m_io.io.b_field.get <> m_aubrac(0).io.b_field.get
    }    
    m_io.io.i_slct.get := m_slct.get.io.o_slct
  }
  m_io.io.b_port <> m_llcross.io.b_s(mem)
  mem = mem + 1

  // GPIO
  m_io.io.b_gpio <> io.b_gpio

  // SPI FLash
  if (p.useSpiFlash) m_io.io.b_spi_flash.get <> io.b_spi_flash.get

  // PS/2 Keyboard
  if (p.usePs2Keyboard) m_io.io.b_ps2_kb.get <> io.b_ps2_kb.get

  // UART
  if (p.nUart > 0) m_io.io.b_uart <> io.b_uart

  // SPI Master
  if (p.nSpi > 0) m_io.io.b_spi <> io.b_spi

  // I2C Master
  if (p.nI2c > 0) m_io.io.b_i2c <> io.b_i2c

  // ******************************
  //            FIELD
  // ******************************
  if (p.useField) {
    for (f <- 0 until p.nField) {
      val w_free = Wire(Vec(2, Bool()))

      if (p.useRom) w_free(0) := m_rom.get.io.b_field.get(f).free else w_free(0) := true.B
      if (p.useRam) w_free(1) := m_ram.get.io.b_field.get(f).free else w_free(1) := true.B
      
      if (p.nAbondance > 0) {
        m_abondance(0).io.b_field.get(f).free := m_llcross.io.b_field.get(f).free & m_io.io.b_field.get(f).free & w_free.asUInt.andR
      } else if (p.nSalers > 0) {
        m_salers(0).io.b_field.get(f).free := m_llcross.io.b_field.get(f).free & m_io.io.b_field.get(f).free & w_free.asUInt.andR
      } else {
        m_aubrac(0).io.b_field.get(f).free := m_llcross.io.b_field.get(f).free & m_io.io.b_field.get(f).free & w_free.asUInt.andR
      }
    }

    for (pa <- 0 until p.nPart) {
      m_pall.get.io.b_rsrc.state(pa).free := true.B
    }
  }  

  // ******************************
  //             REPORT
  // ******************************
  m_llcross.report("PLATFORM LAST-LEVEL")

  // ******************************
  //             DEBUG
  // ******************************  
  if (p.debug) {
    // ------------------------------
    //            SIGNALS
    // ------------------------------
    for (a <- 0 until p.nAubrac) {
      io.o_dbg.get.aubrac(a) := m_aubrac(a).io.o_dbg.get
    }
    for (s <- 0 until p.nSalers) {
      io.o_dbg.get.salers(s) := m_salers(s).io.o_dbg.get
    }
    for (a <- 0 until p.nAbondance) {
      io.o_dbg.get.abondance(a) := m_abondance(a).io.o_dbg.get
    }

    dontTouch(m_llcross.io.b_m)
    dontTouch(m_llcross.io.b_s)

    // ------------------------------
    //       EXECUTION TRACKER
    // ------------------------------
    var e: Int = 0

    for (a <- 0 until p.nAubrac) {
      io.o_etd.get(e) := m_aubrac(a).io.o_etd.get
      e = e + 1
    }

    for (s <- 0 until p.nSalers) {
      for (c <- 0 until p.pSalers(s).nCommit) {
        io.o_etd.get(e) := m_salers(s).io.o_etd.get(c)
        e = e + 1
      }
    }

    for (a <- 0 until p.nAbondance) {
      for (c <- 0 until p.pAbondance(a).nCommit) {
        io.o_etd.get(e) := m_abondance(a).io.o_etd.get(c)
        e = e + 1
      }
    }
  } 
}

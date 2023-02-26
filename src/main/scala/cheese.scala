/*
 * File: cheese.scala                                                          *
 * Created Date: 2023-02-26 09:45:59 am                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2023-02-26 09:49:40 am                                       *
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
import herd.common.dome._
import herd.common.mem.mb4s._
import herd.common.mem.axi4._
import herd.common.mem.ram._
import herd.core.aubrac._
import herd.core.aubrac.common._
import herd.core.abondance.{Abondance}
import herd.io.periph.uart.{UartIO}
import herd.io.periph.ps2.{Ps2IO}
import herd.io.periph.spi.{SpiIO}
import herd.io.periph.i2c.{I2cIO}
import herd.io.pltf._


class Cheese (p: CheeseParams) extends Module {
  require ((p.pAubrac.size > 0) != (p.pAbondance.size > 0), "Only one type of core is possible.")
  require ((p.pAubrac.size < 2),     "Only one Aubrac core is possible.")
  require ((p.pAbondance.size < 2),  "Only one Abondance core is possible.")
  
  val io = IO(new Bundle {
    val b_gpio = new BiDirectIO(UInt(p.nGpio.W))
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
    val o_dfp = if (p.debug) Some(Output(new CheeseDfpBus(p))) else None
    val o_etd = if (p.debug) Some(Output(Vec(p.nCommit, new EtdBus(p.nHart, p.nAddrBit, p.nInstrBit)))) else None
  })  
  
  val m_aubrac = for (po <- p.pAubrac) yield {
    val m_aubrac = Module(new Aubrac(po))
    m_aubrac
  } 
  val m_abondance = for (pg <- p.pAbondance) yield {
    val m_abondance = Module(new Abondance(pg))
    m_abondance
  } 
  val m_llcross = Module(new Mb4sCrossbar(p.pLLCross))
  val m_boot = Module(new Mb4sDataRam(p.pBoot))
  val m_rom = if (p.useRom) Some(Module(new Mb4sDataRam(p.pRom))) else None 
  val m_ram = if (p.useRom) Some(Module(new Mb4sDataRam(p.pRam))) else None 
  val m_io = Module(new IOPltf(p.pIO))
  val m_pall = if (p.useDome) Some(Module(new Part2Rsrc(1, p.nDome, p.nPart, p.nPart))) else None

  // ******************************
  //              CORE
  // ******************************  
  // Interrupts
  if (p.useCeps) {
    for (tl <- 0 until p.nCepsTrapLvl) {
      if (p.pAbondance.size > 0) {
        m_abondance(0).io.i_irq_lei.get(tl) := m_io.io.o_irq_lei.get(0)(tl)
        m_abondance(0).io.i_irq_lsi.get(tl) := m_io.io.o_irq_lsi.get(0)(tl)
      } else {
        m_aubrac(0).io.i_irq_lei.get(tl) := m_io.io.o_irq_lei.get(0)(tl)
        m_aubrac(0).io.i_irq_lsi.get(tl) := m_io.io.o_irq_lsi.get(0)(tl)
      }
    }
  } else {
    if (p.pAbondance.size > 0) {
      m_abondance(0).io.i_irq_mei.get := m_io.io.o_irq_mei.get(0)
      m_abondance(0).io.i_irq_msi.get := m_io.io.o_irq_msi.get(0)
    } else {
      m_aubrac(0).io.i_irq_mei.get := m_io.io.o_irq_mei.get(0)
      m_aubrac(0).io.i_irq_msi.get := m_io.io.o_irq_msi.get(0)
    }
  }

  // ******************************
  //           DOME SELECT
  // ******************************  
  val m_slct = if (p.useDome) Some(Module(new StaticSlct(p.nDome, p.nPart, 1))) else None

  if (p.useDome) {
    if (p.pAbondance.size > 0) {
      m_pall.get.io.b_part <> m_abondance(0).io.b_pall.get
    } else {
      m_pall.get.io.b_part <> m_aubrac(0).io.b_pall.get
    }
    for (d <- 0 until p.nDome) {
      m_slct.get.io.i_weight(d) := m_pall.get.io.b_rsrc.weight(d)
    }
  }
  
  // ******************************
  //        BUS INTERCONNECT
  // ******************************  
  var mem: Int = 0
  
  if (p.useDome) {
    if (p.pAbondance.size > 0) {
      m_llcross.io.b_dome.get <> m_abondance(0).io.b_dome.get
    } else {
      m_llcross.io.b_dome.get <> m_aubrac(0).io.b_dome.get
    }    
    m_llcross.io.i_slct_req.get := m_slct.get.io.o_slct
    m_llcross.io.i_slct_write.get := m_slct.get.io.o_slct
    m_llcross.io.i_slct_read.get := m_slct.get.io.o_slct
  }

  var v_mllcross: Int = 0
  for (g <- 0 until p.pAbondance.size) {
    if (p.pAbondance(g).useL2) {
      m_llcross.io.b_m(v_mllcross + 0) <> m_abondance(0).io.b_mem.get
    } else {
      if (p.pAbondance(g).useL1D) {
        m_llcross.io.b_m(v_mllcross + 0) <> m_abondance(0).io.b_dmem.get
        v_mllcross = v_mllcross + 1
      } else {
        m_llcross.io.b_m(v_mllcross + 0) <> m_abondance(0).io.b_d0mem.get
        m_llcross.io.b_m(v_mllcross + 1) <> m_abondance(0).io.b_d1mem.get
        v_mllcross = v_mllcross + 2
      }
      m_llcross.io.b_m(v_mllcross + 0) <> m_abondance(0).io.b_imem.get
      v_mllcross = v_mllcross + 1
    }
  }

  for (o <- 0 until p.pAubrac.size) {
    if (p.pAubrac(o).useL2) {
      m_llcross.io.b_m(v_mllcross + 0) <> m_aubrac(0).io.b_mem.get
    } else {
      m_llcross.io.b_m(v_mllcross + 0) <> m_aubrac(0).io.b_dmem.get
      m_llcross.io.b_m(v_mllcross + 1) <> m_aubrac(0).io.b_imem.get
    }      
  }

  // ******************************
  //         ROM, RAM & I/Os
  // ******************************
  // ------------------------------
  //              BOOT
  // ------------------------------
  if (p.useDome) {
    if (p.pAbondance.size > 0) {
      m_boot.io.b_dome.get <> m_abondance(0).io.b_dome.get        
    } else {
      m_boot.io.b_dome.get <> m_aubrac(0).io.b_dome.get        
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
    if (p.useDome) {
      if (p.pAbondance.size > 0) {
        m_rom.get.io.b_dome.get <> m_abondance(0).io.b_dome.get        
      } else {
        m_rom.get.io.b_dome.get <> m_aubrac(0).io.b_dome.get        
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
    if (p.useDome) {
      if (p.pAbondance.size > 0) {
        m_ram.get.io.b_dome.get <> m_abondance(0).io.b_dome.get
      } else {
        m_ram.get.io.b_dome.get <> m_aubrac(0).io.b_dome.get
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
  if (p.useDome) {
    if (p.pAbondance.size > 0) {
      m_io.io.b_dome.get <> m_abondance(0).io.b_dome.get
    } else {
      m_io.io.b_dome.get <> m_aubrac(0).io.b_dome.get
    }    
    m_io.io.i_slct.get := m_slct.get.io.o_slct
  }
  m_io.io.b_port <> m_llcross.io.b_s(mem)
  mem = mem + 1

  // GPIO
  m_io.io.b_gpio.in := 0.U
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
  //             DOME
  // ******************************
  if (p.useDome) {
    for (d <- 0 until p.nDome) {
      val w_free = Wire(Vec(2, Bool()))

      if (p.useRom) w_free(0) := m_rom.get.io.b_dome.get(d).free else w_free(0) := true.B
      if (p.useRam) w_free(1) := m_ram.get.io.b_dome.get(d).free else w_free(1) := true.B
      
      if (p.pAbondance.size > 0) {
        m_abondance(0).io.b_dome.get(d).free := m_llcross.io.b_dome.get(d).free & m_io.io.b_dome.get(d).free & w_free.asUInt.andR
      } else {
        m_aubrac(0).io.b_dome.get(d).free := m_llcross.io.b_dome.get(d).free & m_io.io.b_dome.get(d).free & w_free.asUInt.andR
      }
    }

    for (pa <- 0 until p.nPart) {
      m_pall.get.io.b_rsrc.state(pa).free := true.B
    }
  }  

  // ******************************
  //             REPORT
  // ******************************
  println("Memory bus size: " + (p.pLLBus.nDataByte * 8) + "-bit.")

  // ******************************
  //             DEBUG
  // ******************************  
  if (p.debug) {
    // ------------------------------
    //            SIGNALS
    // ------------------------------
    for (no <- 0 until p.nAubrac) {
      io.o_dbg.get.aubrac(no) := m_aubrac(no).io.o_dbg.get
    }
    for (ng <- 0 until p.nAbondance) {
      io.o_dbg.get.abondance(ng) := m_abondance(ng).io.o_dbg.get
    }

    dontTouch(m_llcross.io.b_m)
    dontTouch(m_llcross.io.b_s)

    // ------------------------------
    //         DATA FOOTPRINT
    // ------------------------------
    for (no <- 0 until p.nAubrac) {
      io.o_dfp.get.aubrac(no) := m_aubrac(no).io.o_dfp.get
    }

    // ------------------------------
    //       EXECUTION TRACKER
    // ------------------------------
    var e: Int = 0

    for (o <- 0 until p.nAubrac) {
      io.o_etd.get(e) := m_aubrac(e).io.o_etd.get
      e = e + 1
    }

    for (g <- 0 until p.nAbondance) {
      for (c <- 0 until p.pAbondance(g).nCommit) {
        io.o_etd.get(e) := m_abondance(g).io.o_etd.get(c)
        e = e + 1
      }
    }
  } 
}

object Cheese extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Cheese(CheeseConfigBase), args)
}

/*
 * File: sim.scala
 * Created Date: 2023-02-26 09:45:59 am                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2023-03-15 08:57:07 am
 * Modified By: Mathieu Escouteloup
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
import herd.core.aubrac.common._
import herd.io.periph.uart.{UartParams, UartConfig}
import herd.io.periph.uart.{UartIO, UartStatusBus, UartConfigBus, UartPortIO, Uart}
import herd.io.periph.ps2.{Ps2IO}
import herd.io.periph.spi.{SpiIO}
import herd.io.periph.i2c.{I2cIO}


class CheeseSim (p: CheeseParams) extends Module {

  def pHostUart: UartParams = new UartConfig (
    debug = true,
    nDataByte = 8,
    useRegMem = false,
    nBufferDepth = 8
  )

  val io = IO(new Bundle {
    val b_gpio = Vec(p.nGpio32b, new BiDirectIO(UInt(32.W)))
    val b_spi_flash = if (p.useSpiFlash) Some(new SpiIO(1)) else None
    val b_ps2_kb = if (p.usePs2Keyboard) Some(new Ps2IO()) else None
    val b_spi = MixedVec(
      for (ps <- p.pIO.pSpi) yield {
        new SpiIO(ps.nSlave)
      }
    )
    val b_i2c = Vec(p.nI2c, new I2cIO())

    val o_host_uart_status = Vec(p.nUart, Output(new UartStatusBus()))
    val i_host_uart_config = Vec(p.nUart, Input(new UartConfigBus()))
    val b_host_uart_port = Vec(p.nUart, new UartPortIO(p, 8))

    val o_dbg = if (p.debug) Some(Output(new CheeseDbgBus(p))) else None
    val o_etd = if (p.debug) Some(Output(Vec(p.nCommit, new EtdBus(p.nHart, p.nAddrBit, p.nInstrBit)))) else None
  })

  val m_cheese = Module(new Cheese(p))

  // ******************************
  //              I/Os
  // ******************************
  io.b_gpio <> m_cheese.io.b_gpio

  // ******************************
  //           INTERFACE
  // ******************************
  // ------------------------------
  //             AXI4
  // ------------------------------
  val w_long = Wire(UInt(128.W))

  w_long := "hfedcba9876543210".U


  dontTouch(w_long)

  // ******************************
  //              HOST
  // ******************************
  if (p.nUart > 0) {
    val m_host_uart = Seq.fill(p.nUart){Module(new Uart(pHostUart))}

    for (u <- 0 until p.nUart) {
      io.o_host_uart_status(u) := m_host_uart(u).io.o_status.get
      m_host_uart(u).io.i_config.get := io.i_host_uart_config(u)
      m_host_uart(u).io.b_port.get <> io.b_host_uart_port(u)
      m_host_uart(u).io.b_uart.rx := m_cheese.io.b_uart(u).tx
      m_cheese.io.b_uart(u).rx := m_host_uart(u).io.b_uart.tx 
    }
  }

  if (p.useSpiFlash) m_cheese.io.b_spi_flash.get <> io.b_spi_flash.get
  if (p.usePs2Keyboard) m_cheese.io.b_ps2_kb.get <> io.b_ps2_kb.get
  if (p.nSpi > 0) m_cheese.io.b_spi <> io.b_spi  
  if (p.nI2c > 0) m_cheese.io.b_i2c <> io.b_i2c  

  // ******************************
  //             DEBUG
  // ******************************
  if (p.debug) {
    // ------------------------------
    //            SIGNALS
    // ------------------------------
    io.o_dbg.get := m_cheese.io.o_dbg.get

    // PS/2 Keyboard
    if (p.usePs2Keyboard) {
      dontTouch(m_cheese.io.b_ps2_kb.get)
    }

    // ------------------------------
    //       EXECUTION TRACKER
    // ------------------------------
    io.o_etd.get := m_cheese.io.o_etd.get
  } 
}

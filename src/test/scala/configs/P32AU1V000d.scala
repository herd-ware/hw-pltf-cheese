/*
 * File: P32AU1V000d.scala
 * Created Date: 2023-02-26 09:45:59 am                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2023-03-01 09:49:52 am
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

import herd.core.aubrac.{AubracParams,AubracConfig}
import herd.core.abondance.{AbondanceParams,AbondanceConfig}
import herd.core.abondance.int.{IntUnitIntf}


object CheeseConfigP32AU1V000d extends CheeseConfig (
  // ******************************
  //             CORE
  // ******************************
  pAubrac = Array(new AubracConfig(
    // ------------------------------
    //            GLOBAL
    // ------------------------------
    debug = true,
    pcBoot = "00001000",
    nAddrBit = 32,
    nDataBit = 32, 

    // ------------------------------
    //            CHAMP
    // ------------------------------
    useChamp = false,
    nChampReg = 4,
    useChampExtMie = true,
    useChampExtFr = false,
    useChampExtCst = false,
    nChampTrapLvl = 2,
    
    nPart = 2,
    nFieldFlushCycle = 10,

    // ------------------------------
    //           FRONT END
    // ------------------------------
    nFetchInstr = 1,
    useIMemSeq = true,
    useIf1Stage = false,
    nFetchBufferDepth = 2,

    // ------------------------------
    //       NEXT-LINE PREDICTOR
    // ------------------------------
    useNlp = true,
    nBtbLine = 8,
    nBhtSet = 8,
    nBhtSetEntry = 128,
    nBhtBit = 2,
    useRsbSpec = true,
    nRsbDepth = 8,

    // ------------------------------
    //           BACK END
    // ------------------------------
    useExtM = true,
    useExtA = false,
    useExtB = false,
    useExtZifencei = true,
    useExtZicbo = false,
    nExStage = 1,
    useMemStage = true,
    useBranchReg = true,

    // ------------------------------
    //              I/Os
    // ------------------------------
    nIOAddrBase = "10000000",
    nScratch = 8,
    nCTimer = 2,

    nUnCacheBase = "18000000",
    nUnCacheByte = "08000000",

    // ------------------------------
    //           L1I CACHE
    // ------------------------------
    useL1I = false,
    nL1INextDataByte = 8,
    nL1INextLatency = 1,

    useL1IPftch = false,
    nL1IPftchEntry = 4,
    nL1IPftchEntryAcc = 1,
    nL1IPftchMemRead = 1,
    nL1IPftchMemWrite = 1,

    nL1IMem = 1,
    nL1IMemReadPort = 1,
    nL1IMemWritePort = 1,

    slctL1IPolicy = "BitPLRU",
    nL1ISet = 4,
    nL1ILine = 4,
    nL1IData = 4,

    // ------------------------------
    //           L1D CACHE
    // ------------------------------
    useL1D = false,
    nL1DNextDataByte = 8,
    nL1DNextLatency = 1,

    useL1DPftch = false,
    nL1DPftchEntry = 4,
    nL1DPftchEntryAcc = 1,
    nL1DPftchMemRead = 1,
    nL1DPftchMemWrite = 1,

    nL1DMem = 1,
    nL1DMemReadPort = 1,
    nL1DMemWritePort = 1,

    slctL1DPolicy = "BitPLRU",
    nL1DSet = 4,
    nL1DLine = 4,
    nL1DData = 4,

    // ------------------------------
    //           L2 CACHE
    // ------------------------------
    useL2 = false,
    nL2NextDataByte = 8,
    useL2ReqReg = true,
    useL2AccReg = false,
    useL2AckReg = false,
    nL2WriteFifoDepth = 2,
    nL2NextFifoDepth = 2,
    nL2NextLatency = 1,

    useL2Pftch = false,
    nL2PftchEntry = 4,
    nL2PftchEntryAcc = 1,
    nL2PftchMemRead = 1,
    nL2PftchMemWrite = 1,

    nL2Mem = 2,
    nL2MemReadPort = 2,
    nL2MemWritePort = 1,

    slctL2Policy = "BitPLRU",
    nL2Set = 4,
    nL2Line = 4,
    nL2Data = 4
  )),
  pAbondance = Array[AbondanceParams](),

  // ******************************
  //            GLOBAL
  // ******************************
  debug = true,    

  // ******************************
  //              I/Os
  // ******************************
  nIOAddrBase = "18000000",
  nPlicPrio = 8,
  nGpio = 64,
  nUart = 1,
  nUartDefCycle = 50,
  nUartDepth = 16,
  nPTimer = 2,
  useSpiFlash = true,
  usePs2Keyboard = true,
  nSpi = 1,
  nSpiSlave = Array(1),
  nSpiFifoDepth = 8,
  nI2c = 1,
  nI2cFifoDepth = 8,

  // ******************************
  //           INTERFACE
  // ******************************
  // ------------------------------
  //             AXI4
  // ------------------------------
  useAxi4 = true,
  nAxi4AddrBase = "08000000",
  nAxi4Byte = "80000000",

  // ******************************
  //             MEMORY
  // ******************************
  // ------------------------------
  //             BOOT
  // ------------------------------
  nBootAddrBase = "00000000",
  nBootByte = "00004000",

  // ------------------------------
  //              ROM
  // ------------------------------
  useRom = true,
  nRomAddrBase = "00004000",
  nRomByte = "00004000",

  // ------------------------------
  //              RAM
  // ------------------------------
  useRam = true,
  nRamAddrBase = "00008000",
  nRamByte = "00004000"
)

object CheeseSimP32AU1V000 extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new CheeseSim(CheeseConfigP32AU1V000d), args)
}
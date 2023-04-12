/*
 * File: C32AB1V021.scala                                                      *
 * Created Date: 2023-02-26 09:45:59 am                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2023-04-12 10:42:34 am                                       *
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

import herd.core.aubrac.{AubracParams,AubracConfig}
import herd.core.salers.{SalersParams,SalersConfig}
import herd.core.abondance.{AbondanceParams,AbondanceConfig}
import herd.core.abondance.int.{IntUnitIntf}


trait CheeseParamsC32AB1V021 extends CheeseParams {
  // ******************************
  //            GLOBAL
  // ******************************
  def debug: Boolean  

  // ******************************
  //             CORE
  // ******************************
  def pAubrac: Array[AubracParams] = Array[AubracParams]()
  def pSalers: Array[SalersParams] = Array[SalersParams]()
  def pAbondance: Array[AbondanceParams] = Array(new AbondanceConfig(
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
    useChamp = true,
    nChampReg = 4,
    useChampExtMie = true,
    useChampExtFr = false,
    useChampExtCst = false,
    nChampTrapLvl = 2,
    
    nFieldFlushCycle = 20,
    nPart = 2,

    // ------------------------------
    //           FRONT END
    // ------------------------------
    nFetchInstr = 2,
    useIMemSeq = true,
    useIf1Stage = false,
    useIf2Stage = true,
    nFetchBufferDepth = 8,  
    useFastJal = true,

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
    nBackPort = 2,
    nCommit = 2,
    nRobEntry = 32,
    nSpecBranch = 2,
    useIdStage = true,

    nIntQueue = 16,
    iIntUnit = Array(
      new IntUnitIntf (
        useAlu = true,
        useAluBypass = true,
        useBru = true,
        useExtB = false,
        useExtZifencei = true,
        useExtZicbo = true,
        useMul = true,
        nMulAddLvl = 4,
        useDiv = false,
        useCsr = false      
      ),
      new IntUnitIntf (
        useAlu = true,
        useAluBypass = false,
        useBru = false,
        useExtB = true,
        useExtZifencei = false,
        useExtZicbo = false,
        useMul = false,
        nMulAddLvl = 3,
        useDiv = true,
        useCsr = true      
      ),
    ),

    useExtA = true,
    nLoadQueue = 8,
    useSpecLoad = true,
    nStoreQueue = 8,
    nMemQueue = 4,
    nHfuQueue = 4,

    nGprPhy = 64,
    nGprReadPhy = 4,
    nGprWritePhy = 2,

    // ------------------------------
    //              I/Os
    // ------------------------------
    nIOAddrBase = "10000000",
    nScratch = 8,
    nCTimer = 2,
    isHpmAct = Array("ALL"),
    hasHpmMap = Array(),

    nUnCacheBase = "18000000",
    nUnCacheByte = "08000000",

    // ------------------------------
    //           L1I CACHE
    // ------------------------------
    useL1I = true,
    nL1INextDataByte = 8,
    nL1INextLatency = 1,

    useL1IPftch = false,
    nL1IPftchEntry = 4,
    nL1IPftchEntryAcc = 1,
    nL1IPftchMemRead = 1,
    nL1IPftchMemWrite = 1,

    nL1IMem = 1,
    nL1IMemReadPort = 2,
    nL1IMemWritePort = 1,

    slctL1IPolicy = "BitPLRU",
    nL1ISet = 4,
    nL1ILine = 4,
    nL1IData = 4,

    // ------------------------------
    //           L1D CACHE
    // ------------------------------
    useL1D = true,
    nL1DNextDataByte = 8,
    nL1DNextLatency = 1,

    useL1DPftch = false,
    nL1DPftchEntry = 4,
    nL1DPftchEntryAcc = 1,
    nL1DPftchMemRead = 1,
    nL1DPftchMemWrite = 1,

    nL1DMem = 1,
    nL1DMemReadPort = 2,
    nL1DMemWritePort = 1,

    slctL1DPolicy = "BitPLRU",
    nL1DSet = 4,
    nL1DLine = 4,
    nL1DData = 4,

    // ------------------------------
    //           L2 CACHE
    // ------------------------------
    useL2 = true,
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
  ))

  // ******************************
  //              I/Os
  // ******************************
  def nIOAddrBase: String = "18000000"
  def nPlicPrio: Int = 8
  def nGpio: Int = 64
  def nUart: Int = 1
  def nUartDefCycle: Int = 50
  def nUartDepth: Int = 16
  def nPTimer: Int = 2
  def useSpiFlash: Boolean = true
  def usePs2Keyboard: Boolean = true
  def nSpi: Int = 1
  def nSpiSlave: Array[Int] = Array(1)
  def nSpiFifoDepth: Int = 8
  def nI2c: Int = 1
  def nI2cFifoDepth: Int = 8

  // ******************************
  //           INTERFACE
  // ******************************
  // ------------------------------
  //             AXI4
  // ------------------------------
  def useAxi4: Boolean = true
  def nAxi4AddrBase: String = "08000000"
  def nAxi4Byte: String = "80000000"

  // ******************************
  //             MEMORY
  // ******************************
  // ------------------------------
  //             BOOT
  // ------------------------------
  def nBootAddrBase: String = "00000000"
  def nBootByte: String = "00040000"

  // ------------------------------
  //              ROM
  // ------------------------------
  def useRom: Boolean = true
  def nRomAddrBase: String = "04000000"
  def nRomByte: String = "00040000"

  // ------------------------------
  //              RAM
  // ------------------------------
  def useRam: Boolean = true
  def nRamAddrBase: String = "08000000"
  def nRamByte: String = "00040000"
}

case class CheeseConfigC32AB1V021 (    
  debug: Boolean
) extends CheeseParamsC32AB1V021

object CheeseC32AB1V021 extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new Cheese(new CheeseConfigC32AB1V021(debug = false)), args)
}

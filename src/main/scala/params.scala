/*
 * File: params.scala                                                          *
 * Created Date: 2023-02-26 09:45:59 am                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2023-02-27 06:11:32 pm                                       *
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

import herd.common.gen._
import herd.common.mem.mb4s._
import herd.common.mem.axi4._
import herd.common.mem.ram._
import herd.core.aubrac._
import herd.core.abondance._
import herd.io.pltf._


trait CheeseParams extends GenParams {    
  // ******************************
  //             CORE
  // ******************************
  def pAubrac: Array[AubracParams]
  def pAbondance: Array[AbondanceParams]

  def nAubrac: Int = pAubrac.size
  def nAbondance: Int = pAbondance.size

  // ******************************
  //            GLOBAL
  // ******************************
  def debug: Boolean
  def nAddrBit: Int = {
    if (pAbondance.size > 0) {
      return pAbondance(0).nAddrBit
    } else {
      return pAubrac(0).nAddrBit
    }    
  }
  def nInstrBit: Int = 32
  def nDataBit: Int = {
    if (pAbondance.size > 0) {
      return pAbondance(0).nDataBit
    } else {
      return pAubrac(0).nDataBit
    }    
  }
  def nHart: Int = nAubrac + nAbondance
  def nCommit: Int = {
    var c: Int = 0

    for (o <- 0 until nAubrac) {
      c = c + 1
    }

    for (g <- 0 until nAbondance) {
      c = c + pAbondance(g).nCommit
    }

    c
  }

  // ******************************
  //            CHAMP
  // ******************************
  def useChamp: Boolean = {
    if (pAbondance.size > 0) {
      return pAbondance(0).useChamp
    } else {
      return pAubrac(0).useChamp
    }    
  }
  def nChampTrapLvl: Int = {
    if (pAbondance.size > 0) {
      return pAbondance(0).nChampTrapLvl
    } else {
      return pAubrac(0).nChampTrapLvl
    }    
  }
  def useDome: Boolean = {
    if (pAbondance.size > 0) {
      return pAbondance(0).useDome
    } else {
      return pAubrac(0).useDome
    }    
  }
  def nDome: Int = {
    if (pAbondance.size > 0) {
      return pAbondance(0).nDome
    } else {
      return pAubrac(0).nDome
    }    
  }
  def multiDome: Boolean = true
  def nPart: Int = {
    if (pAbondance.size > 0) {
      return pAbondance(0).nPart
    } else {
      return pAubrac(0).nPart
    }    
  }

  // ******************************
  //              BUS
  // ******************************
  def pLLArray: Array[Mb4sParams] = {
    var pbus = Array[Mb4sParams]()

    for (g <- 0 until pAbondance.size) {
      if (pAbondance(g).useL2) {
        pbus = pbus :+ pAbondance(g).pLLBus
      } else {
        if (pAbondance(g).useL1D) {
          pbus = pbus :+ pAbondance(g).pLLDBus
        } else {
          pbus = pbus :+ pAbondance(g).pLLDBus
          pbus = pbus :+ pAbondance(g).pLLDBus
        }
        pbus = pbus :+ pAbondance(g).pLLIBus
      } 
    }

    for (o <- 0 until pAubrac.size) {
      if (pAubrac(o).useL2) {
        pbus = pbus :+ pAubrac(o).pLLBus
      } else {
        pbus = pbus :+ pAubrac(o).pLLDBus
        pbus = pbus :+ pAubrac(o).pLLIBus
      }      
    }

    return pbus
  }
  def pLLBus: Mb4sParams = MB4S.node(pLLArray, true)

  // ******************************
  //              I/Os
  // ******************************
  def nIOAddrBase: String
  def nPlicPrio: Int
  def nGpio: Int
  def nUart: Int
  def nUartDefCycle: Int
  def nUartDepth: Int
  def nPTimer: Int
  def useSpiFlash: Boolean 
  def usePs2Keyboard: Boolean
  def nSpi: Int
  def nSpiSlave: Array[Int]
  def nSpiFifoDepth: Int
  def nI2c: Int 
  def nI2cFifoDepth: Int

  def pIO: IOPltfParams = new IOPltfConfig (
    pPort               = Array(pLLBus)       ,

    debug               = debug               ,
    nAddrBit            = nAddrBit            ,
    nAddrBase           = nIOAddrBase         ,

    nChampTrapLvl       = nChampTrapLvl        ,

    useReqReg           = true                ,
    nPlicPrio           = nPlicPrio           ,
    nGpio               = nGpio               ,
    nUart               = nUart               ,
    nUartFifoDepth      = nUartDepth          ,
    nPTimer             = nPTimer             ,
    useSpiFlash         = useSpiFlash         ,
    usePs2Keyboard      = usePs2Keyboard      ,
    nSpi                = nSpi                ,
    nSpiSlave           = nSpiSlave           ,
    nSpiFifoDepth       = nSpiFifoDepth       ,
    nI2c                = nI2c                ,
    nI2cFifoDepth       = nI2cFifoDepth
  )

  // ******************************
  //           INTERFACE
  // ******************************
  // ------------------------------
  //             AXI4
  // ------------------------------
  def useAxi4: Boolean
  def nAxi4AddrBase: String
  def nAxi4Byte: String
  
  def pAxi4Mem: Mb4sMemParams = new Mb4sMemConfig (
    pPort = Array(pLLBus),
  
    nAddrBase = nAxi4AddrBase,
    nByte = nAxi4Byte
  )

  def pAxi4: Axi4Params = new Axi4Config (
    debug = pLLBus.debug,
    nAddrBit = pLLBus.nAddrBit,
    nDataByte = pLLBus.nDataByte,
    nId = 1
  )

  // ******************************
  //            MEMORY
  // ******************************
  // ------------------------------
  //             BOOT
  // ------------------------------
  def nBootAddrBase: String
  def nBootByte: String

  def pBoot: Mb4sRamParams = new Mb4sRamConfig (
    pPort     = Array(pLLBus, pLLBus)   ,
    debug     = debug                   ,
    initFile  = "boot.mem"              ,
    isRom     = true                    ,
    nAddrBase = nBootAddrBase           ,
    useReqReg = false                   ,
    nByte     = nBootByte
  ) 

  // ------------------------------
  //              ROM
  // ------------------------------
  def useRom: Boolean
  def nRomAddrBase: String
  def nRomByte: String

  def pRom: Mb4sRamParams = new Mb4sRamConfig (
    pPort     = Array(pLLBus, pLLBus)   ,
    debug     = debug                   ,
    initFile  = "rom.mem"               ,
    isRom     = true                    ,
    nAddrBase = nRomAddrBase            ,
    useReqReg = false                   ,
    nByte     = nRomByte
  )  

  // ------------------------------
  //              RAM
  // ------------------------------
  def useRam: Boolean
  def nRamAddrBase: String
  def nRamByte: String

  def pRam: Mb4sRamParams = new Mb4sRamConfig (
    pPort     = Array(pLLBus, pLLBus)   ,
    debug     = debug                   ,
    initFile  = ""                      ,
    isRom     = false                   ,
    nAddrBase = nRamAddrBase            ,
    useReqReg = false                   ,  
    nByte     = nRamByte                ,
  )

  // ******************************
  //          INTERCONNECT
  // ------------------------------
  def pLLCross: Mb4sCrossbarParams = new Mb4sCrossbarConfig (
    pMaster = pLLArray                  ,
    useMem = true                       ,
    pMem        = {
      var pmem = Array[Mb4sMemParams]()
      pmem = pmem :+ pBoot
      pmem = pmem :+ pBoot
      if (useRom) {
        pmem = pmem :+ pRom
        pmem = pmem :+ pRom
      }
      if (useRam) {
        pmem = pmem :+ pRam
        pmem = pmem :+ pRam
      }
      pmem = pmem :+ pIO
      if (useAxi4) {
//        pmem = pmem :+ pAxi4Mem
      }
      pmem
    }                                   ,
    nDefault = 0                        ,
    nBus = 0                            ,
    
    debug = debug                       ,  
    multiDome = true                    ,
    nDepth = 4                          ,
    useDirect = false
  )
}

case class CheeseConfig (    
  // ******************************
  //             CORE
  // ******************************
  pAubrac: Array[AubracParams],
  pAbondance: Array[AbondanceParams],

  // ******************************
  //            GLOBAL
  // ******************************
  debug: Boolean,

  // ******************************
  //              I/Os
  // ******************************
  nIOAddrBase: String,
  nPlicPrio: Int,
  nGpio: Int,
  nUart: Int,
  nUartDefCycle: Int,
  nUartDepth: Int,
  nPTimer: Int,
  useSpiFlash: Boolean,
  usePs2Keyboard: Boolean,
  nSpi: Int,
  nSpiSlave: Array[Int],
  nSpiFifoDepth: Int,
  nI2c: Int,
  nI2cFifoDepth: Int,

  // ******************************
  //           INTERFACE
  // ******************************
  // ------------------------------
  //             AXI4
  // ------------------------------
  useAxi4: Boolean,
  nAxi4AddrBase: String,
  nAxi4Byte: String,

  // ******************************
  //             MEMORY
  // ******************************
  // ------------------------------
  //             BOOT
  // ------------------------------
  nBootAddrBase: String,
  nBootByte: String,

  // ------------------------------
  //              ROM
  // ------------------------------
  useRom: Boolean,
  nRomAddrBase: String,
  nRomByte: String,

  // ------------------------------
  //              RAM
  // ------------------------------
  useRam: Boolean,
  nRamAddrBase: String,
  nRamByte: String
) extends CheeseParams

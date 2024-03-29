/*
 * File: bus.scala                                                             *
 * Created Date: 2023-02-26 09:45:59 am                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2023-04-12 10:24:09 am                                       *
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

import herd.core.aubrac.{AubracDbgBus}
import herd.core.abondance.{AbondanceDbgBus}
import herd.core.salers.{SalersDbgBus}


// ******************************
//             DEBUG
// ******************************
class CheeseDbgBus (p: CheeseParams) extends Bundle {
  val aubrac = MixedVec(
    for (pa <- p.pAubrac) yield {
      new AubracDbgBus(pa)
    }
  )
  val salers = MixedVec(
    for (ps <- p.pSalers) yield {
      new SalersDbgBus(ps)
    }
  )
  val abondance = MixedVec(
    for (pa <- p.pAbondance) yield {
      new AbondanceDbgBus(pa)
    }
  )
}           
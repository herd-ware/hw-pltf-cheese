/*
 * File: bus.scala                                                             *
 * Created Date: 2023-02-26 09:45:59 am                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2023-03-02 05:59:35 pm                                       *
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


// ******************************
//             DEBUG
// ******************************
class CheeseDbgBus (p: CheeseParams) extends Bundle {
  val aubrac = MixedVec(
    for (po <- p.pAubrac) yield {
      new AubracDbgBus(po)
    }
  )
  val abondance = MixedVec(
    for (pg <- p.pAbondance) yield {
      new AbondanceDbgBus(pg)
    }
  )
}           
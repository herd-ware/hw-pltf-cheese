/*
 * File: C32AU1V020d.scala
 * Created Date: 2023-02-26 09:45:59 am                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2023-03-15 08:34:50 am
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


object CheeseSimC32AU1V020 extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new CheeseSim(new CheeseConfigC32AU1V020(debug = true)), args)
}
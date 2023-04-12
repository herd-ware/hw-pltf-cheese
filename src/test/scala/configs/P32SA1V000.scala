/*
 * File: P32SA1V000.scala                                                      *
 * Created Date: 2023-02-26 09:45:59 am                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2023-04-12 11:37:09 am                                       *
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


object CheeseSimP32SA1V000 extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new CheeseSim(new CheeseConfigP32SA1V000(debug = true)), args)
}
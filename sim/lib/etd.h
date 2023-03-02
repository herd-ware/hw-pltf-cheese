/*
 * File: etd.h
 * Created Date: 2023-02-26 09:45:59 am                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2023-02-26 08:58:38 pm
 * Modified By: Mathieu Escouteloup
 * -----                                                                       *
 * License: See LICENSE.md                                                     *
 * Copyright (c) 2023 HerdWare                                                 *
 * -----                                                                       *
 * Description:                                                                *
 */


#ifndef _ETD_
#define _ETD_ 

#include <stdlib.h>
#include <stdio.h>
#include "VCheeseSim.h"

#include <iostream>
#include <iomanip>
#include <fstream>
using namespace std;

#include "configs.h"


#define ETD_TRACE(commit) \
if (dut->io_o_etd_##commit##_done == 1) {                                             \
  f_etd << setfill('0') << setw(8) << hex << dut->io_o_etd_##commit##_hart << " ";    \
  f_etd << setfill('0') << setw(8) << hex << dut->io_o_etd_##commit##_pc << " ";      \
  f_etd << setfill('0') << setw(8) << hex << dut->io_o_etd_##commit##_instr << " ";   \
  f_etd << setfill('0') << setw(8) << dec << dut->io_o_etd_##commit##_tstart << " ";  \
  f_etd << setfill('0') << setw(8) << dec << dut->io_o_etd_##commit##_tend << " ";    \
  f_etd << setfill('0') << setw(8) << hex << dut->io_o_etd_##commit##_daddr << " ";   \
  f_etd << "\n";                                                                      \
}  

#define INSERT_NOPS_1   nop; INSERT_NOPS_0
#define ETD_TRACE_1 ETD_TRACE(0)
#define ETD_TRACE_2 ETD_TRACE_1;  ETD_TRACE(1)
#define ETD_TRACE_3 ETD_TRACE_2;  ETD_TRACE(2)
#define ETD_TRACE_4 ETD_TRACE_3;  ETD_TRACE(3)
#define ETD_TRACE_5 ETD_TRACE_4;  ETD_TRACE(4)
#define ETD_TRACE_6 ETD_TRACE_5;  ETD_TRACE(5)
#define ETD_TRACE_7 ETD_TRACE_6;  ETD_TRACE(6)
#define ETD_TRACE_8 ETD_TRACE_7;  ETD_TRACE(7)

#define ETD_NTRACE_(commit) ETD_TRACE_##commit
#define ETD_NTRACE(commit) ETD_NTRACE_(commit)


void etd_init_trace(char *file);
void etd_write_trace(VCheeseSim *dut);
void etd_close_trace();

#endif
/*
 * File: hpc.h
 * Created Date: 2023-02-26 09:45:59 am                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2023-04-03 01:12:28 pm
 * Modified By: Mathieu Escouteloup
 * -----                                                                       *
 * License: See LICENSE.md                                                     *
 * Copyright (c) 2023 HerdWare                                                 *
 * -----                                                                       *
 * Description:                                                                *
 */


#ifndef _HPC_
#define _HPC_ 

#include <stdlib.h>
#include <stdio.h>
#include "VCheeseSim.h"

#include "configs.h"


#define HPC_DISPLAY(core, num) \
cout << "------------------------------" << endl; \
cout << "CORE: " #core " " #num "" << endl; \
cout << "------------------------------" << endl; \
cout << "ALU instructions: " << dut->io_o_dbg_##core##_##num##_hpc_alu << endl; \
cout << "BRU instructions: " << dut->io_o_dbg_##core##_##num##_hpc_bru << endl; \
cout << "Cycles: " << dut->io_o_dbg_##core##_##num##_hpc_cycle << endl; \
cout << "Retired instructions: " << dut->io_o_dbg_##core##_##num##_hpc_instret << endl; \
cout << "L1I hits: " << dut->io_o_dbg_##core##_##num##_hpc_l1ihit << endl; \
cout << "L1I misses: " << dut->io_o_dbg_##core##_##num##_hpc_l1imiss << endl; \
cout << "L1I prefetches: " << dut->io_o_dbg_##core##_##num##_hpc_l1ipftch << endl; \
cout << "L1D hits: " << dut->io_o_dbg_##core##_##num##_hpc_l1dhit << endl; \
cout << "L1D misses: " << dut->io_o_dbg_##core##_##num##_hpc_l1dmiss << endl; \
cout << "L1D prefetches: " << dut->io_o_dbg_##core##_##num##_hpc_l1dpftch << endl; \
cout << "L2 hits: " << dut->io_o_dbg_##core##_##num##_hpc_l2hit << endl; \
cout << "L2 misses: " << dut->io_o_dbg_##core##_##num##_hpc_l2miss << endl; \
cout << "L2 prefetches: " << dut->io_o_dbg_##core##_##num##_hpc_l2pftch << endl; \
cout << "Load instructions: " << dut->io_o_dbg_##core##_##num##_hpc_ld << endl; \
cout << "Read cycle instructions: " << dut->io_o_dbg_##core##_##num##_hpc_rdcycle << endl; \
cout << "Store instructions: " << dut->io_o_dbg_##core##_##num##_hpc_st << endl; \
cout << "Time: " << dut->io_o_dbg_##core##_##num##_hpc_time << endl; \
cout << "Function call instructions: " << dut->io_o_dbg_##core##_##num##_hpc_call << endl; \
cout << "Function ret instructions: " << dut->io_o_dbg_##core##_##num##_hpc_ret << endl; \
cout << "JAL instructions: " << dut->io_o_dbg_##core##_##num##_hpc_jal << endl; \
cout << "JALR instructions: " << dut->io_o_dbg_##core##_##num##_hpc_jalr << endl; \
cout << "Cache flush instructions: " << dut->io_o_dbg_##core##_##num##_hpc_cflush << endl; \
cout << "Source dependency wait cycles: " << dut->io_o_dbg_##core##_##num##_hpc_srcdep << endl; \
cout << "------------------------------" << endl;



#define HPC_DISPLAY_0(core)
#define HPC_DISPLAY_1(core) HPC_DISPLAY(core, 0)
#define HPC_DISPLAY_2(core) HPC_DISPLAY_0(core); HPC_DISPLAY(core, 1)
#define HPC_DISPLAY_3(core) HPC_DISPLAY_1(core); HPC_DISPLAY(core, 2)
#define HPC_DISPLAY_4(core) HPC_DISPLAY_2(core); HPC_DISPLAY(core, 3)

#define HPC_DISPLAY_N_(core,num) HPC_DISPLAY_##num(core)
#define HPC_DISPLAY_N(core,num) HPC_DISPLAY_N_(core,num)

#endif
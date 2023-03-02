/*
 * File: etd.cpp
 * Created Date: 2023-02-26 09:45:59 am                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2023-02-26 08:58:46 pm
 * Modified By: Mathieu Escouteloup
 * -----                                                                       *
 * License: See LICENSE.md                                                     *
 * Copyright (c) 2023 HerdWare                                                 *
 * -----                                                                       *
 * Description:                                                                *
 */


#include "etd.h"


ofstream f_etd;

void etd_init_trace(char *file) {
  f_etd.open(file);
}

void etd_write_trace(VCheeseSim *dut) {
  ETD_NTRACE(NCOMMIT)
}

void etd_close_trace() {
  f_etd.close();
}
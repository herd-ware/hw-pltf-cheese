/*
 * File: configs.h
 * Created Date: 2023-02-26 09:45:59 am                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2023-04-12 11:48:59 am
 * Modified By: Mathieu Escouteloup
 * -----                                                                       *
 * License: See LICENSE.md                                                     *
 * Copyright (c) 2023 HerdWare                                                 *
 * -----                                                                       *
 * Description:                                                                *
 */


#ifndef CONFIG_H
#define CONFIG_H

// ******************************
//       CORE CONFIGURATION
// ******************************
// ------------------------------
//        CONFIG P32AU1V000
// ------------------------------
#ifdef CONFIG_P32AU1V000
  #define CORE_MAIN aubrac
  #define CORE_AUBRAC 1
  #define CORE_SALERS 0
  #define CORE_ABONDANCE 0
  #define NDATABIT 32
  #define NCOMMIT 1
  #define NCORECOMMIT 1
// ------------------------------
//        CONFIG C32AU1V000
// ------------------------------
#elif CONFIG_C32AU1V000
  #define CORE_MAIN aubrac
  #define CORE_AUBRAC 1
  #define CORE_SALERS 0
  #define CORE_ABONDANCE 0
  #define NDATABIT 32
  #define NCOMMIT 1
  #define NCORECOMMIT 1
// ------------------------------
//        CONFIG P32AU1V020
// ------------------------------
#elif CONFIG_P32AU1V020
  #define CORE_MAIN aubrac
  #define CORE_AUBRAC 1
  #define CORE_SALERS 0
  #define CORE_ABONDANCE 0
  #define NDATABIT 32
  #define NCOMMIT 1
  #define NCORECOMMIT 1
// ------------------------------
//        CONFIG C32AU1V020
// ------------------------------
#elif CONFIG_C32AU1V020
  #define CORE_MAIN aubrac
  #define CORE_AUBRAC 1
  #define CORE_SALERS 0
  #define CORE_ABONDANCE 0
  #define NDATABIT 32
  #define NCOMMIT 1
  #define NCORECOMMIT 1
// ------------------------------
//        CONFIG C32AU1V021
// ------------------------------
#elif CONFIG_C32AU1V021
  #define CORE_MAIN aubrac
  #define CORE_AUBRAC 1
  #define CORE_SALERS 0
  #define CORE_ABONDANCE 0
  #define NDATABIT 32
  #define NCOMMIT 1
  #define NCORECOMMIT 1
// ------------------------------
//        CONFIG P32SA1V000
// ------------------------------
#elif CONFIG_P32SA1V000
  #define CORE_MAIN salers
  #define CORE_AUBRAC 0
  #define CORE_SALERS 1
  #define CORE_ABONDANCE 0
  #define NDATABIT 32
  #define NCOMMIT 2
  #define NCORECOMMIT 2
// ------------------------------
//        CONFIG P32AB1V000
// ------------------------------
#elif CONFIG_P32AB1V000
  #define CORE_MAIN abondance
  #define CORE_AUBRAC 0
  #define CORE_SALERS 0
  #define CORE_ABONDANCE 1
  #define NDATABIT 32
  #define NCOMMIT 2
  #define NCORECOMMIT 2
// ------------------------------
//        CONFIG C32AB1V000
// ------------------------------
#elif CONFIG_C32AB1V000
  #define CORE_MAIN abondance
  #define CORE_AUBRAC 0
  #define CORE_SALERS 0
  #define CORE_ABONDANCE 1
  #define NDATABIT 32
  #define NCOMMIT 2
  #define NCORECOMMIT 2
// ------------------------------
//        CONFIG P32AB1V020
// ------------------------------
#elif CONFIG_P32AB1V020
  #define CORE_MAIN abondance
  #define CORE_AUBRAC 0
  #define CORE_SALERS 0
  #define CORE_ABONDANCE 1
  #define NDATABIT 32
  #define NCOMMIT 2
  #define NCORECOMMIT 2
// ------------------------------
//        CONFIG C32AB1V020
// ------------------------------
#elif CONFIG_C32AB1V020
  #define CORE_MAIN abondance
  #define CORE_AUBRAC 0
  #define CORE_SALERS 0
  #define CORE_ABONDANCE 1
  #define NDATABIT 32
  #define NCOMMIT 2
  #define NCORECOMMIT 2
// ------------------------------
//        CONFIG C32AB1V021
// ------------------------------
#elif CONFIG_C32AB1V021
  #define CORE_MAIN abondance
  #define CORE_AUBRAC 0
  #define CORE_SALERS 0
  #define CORE_ABONDANCE 1
  #define NDATABIT 32
  #define NCOMMIT 2
  #define NCORECOMMIT 2
// ------------------------------
//            DEFAULT
// ------------------------------
#else
  #define CORE_MAIN none
  #define CORE_AUBRAC 0
  #define CORE_SALERS 0
  #define CORE_ABONDANCE 0
  #define NDATABIT 32
  #define NCOMMIT 0
  #define NCORECOMMIT 0
#endif

#define DBG_CORE_SIGNAL(core, num, signal) DBG_CORE_SIGNAL_(core, num, signal)
#define DBG_CORE_SIGNAL_(core, num, signal) \
  dut->io_o_dbg_##core##_##num##_##signal

#endif
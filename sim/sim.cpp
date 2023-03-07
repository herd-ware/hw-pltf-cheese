/*
 * File: sim.cpp
 * Created Date: 2023-02-26 09:45:59 am                                        *
 * Author: Mathieu Escouteloup                                                 *
 * -----                                                                       *
 * Last Modified: 2023-03-06 10:13:06 am
 * Modified By: Mathieu Escouteloup
 * -----                                                                       *
 * License: See LICENSE.md                                                     *
 * Copyright (c) 2023 HerdWare                                                 *
 * -----                                                                       *
 * Description:                                                                *
 */


#include <stdlib.h>
#include <stdio.h>
#include "VCheeseSim.h"
#include "verilated.h"
#include "verilated_vcd_c.h"
#include "svdpi.h"
#include "VCheeseSim__Dpi.h"
#include <time.h>

#include <iostream>
#include <iomanip>
#include <fstream>
using namespace std;

#include "lib/configs.h"
#include "lib/etd.h"

#define TRIGGER_DELAY 100
#define RESET_DELAY 50

#define GPIOA_BIT_UARTW   27
#define GPIOA_BIT_CYCLE   29
#define GPIOA_BIT_INSTRET 30
#define GPIOA_BIT_END     31


int main(int argc, char **argv) {
  // ******************************
  //             INPUTS
  // ******************************
  char* bootfile;       // .hex format
  char* romfile;        // .hex format
  char* vcdfile;
  char* uartfile;
  char* etdfile;

  int nuartcycle = 50;
  int ntrigger = 0;
  int nreset = 0;
  int ninst = 0;

  bool use_rom = false;
  bool use_vcd = false;
  bool use_test = false;
  bool use_trigger = false;
  bool use_ninst = false;
  bool use_reset = false;
  bool use_uart_in = false;
  bool use_uart_out = false;
  bool use_etd = false;

  for (int a = 1; a < argc; a++) {
    string arg = argv[a];
    if (arg == "--boot") {
      bootfile = argv[a + 1];
      a++;
    }
    if (arg == "--rom") {
      use_rom = true;
      romfile = argv[a + 1];
      a++;
    }
    if (arg == "--vcd") {
      use_vcd = true;
      vcdfile = argv[a + 1];
      a++;
    }
    if (arg == "--test") {
      use_test = true;
    }
    if (arg == "--trigger") {
      use_trigger = true;
      ntrigger = atoi(argv[a + 1]);
      a++;
    }
    if (arg == "--ninst") {
      use_ninst = true;
      ninst = atoi(argv[a + 1]);
      a++;
    }
    if (arg == "--reset") {
      use_reset = true;
      nreset = atoi(argv[a + 1]);
      a++;
    }
    if (arg == "--uart-in") {
      use_uart_in = true;
      uartfile = argv[a + 1];
      a++;
    }
    if (arg == "--uart-cycle") {
      use_uart_out = true;
      nuartcycle = atoi(argv[a + 1]);
      a++;
    }
    if (arg == "--etd") {
      use_etd = true;
      etdfile = argv[a + 1];
      a++;
    }
  }

  // ******************************
  //    SIMULATION CONFIGURATION
  // ******************************
  time_t test_time = time(NULL);

	// Initialize Verilators variables
	Verilated::commandArgs(argc, argv);

  // Create an instance of our module under test
	VCheeseSim *dut = new VCheeseSim;

  // Generate VCD
  Verilated::traceEverOn(true);
  VerilatedVcdC* dut_trace = new VerilatedVcdC;
  dut->trace(dut_trace, 99);
  if (use_vcd) {
    dut_trace->open(vcdfile);
  }

	// Test variables
  int clock = 0;      // Clock cycle since start
  bool end = false;   // Test end
  int result = -1;    // SW result
  int cycle = 0;      // Cycles
  int instret = 0;    // Retired instructions

  if (use_etd) {
    etd_init_trace(etdfile);
  }

  // ******************************
  //           INITIATION
  // ******************************
  // ------------------------------
  //            MEMORY
  // ------------------------------
  // BOOT
  // Call task to initialize memory
  svSetScope(svGetScopeFromName("TOP.CheeseSim.m_cheese.m_boot.m_ram.m_ram"));
  // Verilated::scopesDump();
  dut->ext_readmemh_byte(bootfile);

  // ROM
  if (use_rom) {
    // Call task to initialize memory
    svSetScope(svGetScopeFromName("TOP.CheeseSim.m_cheese.m_rom.m_ram.m_ram"));
    // Verilated::scopesDump();
    dut->ext_readmemh_byte(romfile);
  }

  // ------------------------------
  //             UART
  // ------------------------------
  dut->io_i_host_uart_config_0_en = 1;
  dut->io_i_host_uart_config_0_is8bit = 1;
  dut->io_i_host_uart_config_0_parity = 1;
  dut->io_i_host_uart_config_0_stop = 1;
  dut->io_i_host_uart_config_0_ncycle = 50;

  dut->io_b_host_uart_port_0_rec_0_ready = 1;

  if (use_uart_in || use_uart_out) {
    dut->io_i_host_uart_config_0_ncycle = nuartcycle;
  }

  ifstream f_uart;   

  f_uart.open(uartfile);

  if (use_uart_in && f_uart.fail()) { 
    cout << "\033[1;31m";
    cout << "Error: UART file does not exist." << endl; 
    cout << "\033[0m";
    return 1;
  }

  // ******************************
  //         DEFAULT SIGNALS
  // ******************************

  // ******************************
  //             RESET
  // ******************************
  for (int i = 0; i < 5; i++) {
		dut->clock = 0;
    dut->reset = 1;
		dut->eval();
    if (use_vcd) {
      dut_trace->dump(clock * 10);
    }  

    dut->clock = 1;
  	dut->eval();
    if (use_vcd) {
      dut_trace->dump(clock * 10 + 5);
    }
    clock = clock + 1;
  }
  dut->reset = 0;

  // ******************************
  //           TEST LOOP
  // ******************************
	while ((!Verilated::gotFinish()) && (end == false)) {
    test_time = time(NULL);
    // ------------------------------
    //          FALLING EDGE
    // ------------------------------
		dut->clock = 0;
		dut->eval();
    if (use_vcd) {
      dut_trace->dump(clock * 10);
    }   

    if (use_etd) {
      etd_write_trace(dut);
    }      

    // ------------------------------
    //          RISING EDGE
    // ------------------------------
		dut->clock = 1;
		dut->eval();
    if (use_vcd) {
      dut_trace->dump(clock * 10 + 5);
    }   

    // ------------------------------
    //             RESET
    // ------------------------------
    if (use_reset && (clock > nreset) && (clock < (nreset + RESET_DELAY))) {
      dut->reset = 1;
    } else {
      dut->reset = 0;
    }

    // ------------------------------
    //             UART
    // ------------------------------
    // ..............................
    //             WRITE
    // ..............................
    if (use_uart_in) {    
      if (!f_uart.eof() && dut->io_o_host_uart_status_0_idle && (dut->io_b_gpio_0_eno & dut->io_b_gpio_0_out & (1 << GPIOA_BIT_UARTW))) {
        string uart_swbyte;
        uint8_t uart_wbyte;

        f_uart >> uart_swbyte;
        uart_wbyte = stoi(uart_swbyte);
        dut->io_b_host_uart_port_0_send_0_valid = 1;
        dut->io_b_host_uart_port_0_send_0_data = (uart_wbyte & 0xff);
      } else {
        dut->io_b_host_uart_port_0_send_0_valid = 0;
      }
    }

    // ..............................
    //             READ
    // ..............................
    if (use_uart_out) {    
      if (dut->io_b_host_uart_port_0_rec_0_valid) {
        cout << dut->io_b_host_uart_port_0_rec_0_data;
      }
    }

    // ------------------------------
    //             END
    // ------------------------------
    // Cycles
    if ((cycle == 0) && (dut->io_b_gpio_0_out & (1 << GPIOA_BIT_CYCLE))) {
      cycle = dut->io_b_gpio_1_out;
    }

    // Instruction retired
    if ((instret == 0) && (dut->io_b_gpio_0_out & (1 << GPIOA_BIT_INSTRET))) {
      instret = dut->io_b_gpio_1_out;
    }

    // SW Trigger
    if (dut->io_b_gpio_0_out & (1 << GPIOA_BIT_END)) {
      end = true;
      result = dut->io_b_gpio_1_out;
    }

    // Test trigger
    if (clock > (ntrigger + TRIGGER_DELAY) && (ntrigger > 0)) {
      end = true;
      result = 0xffffffff;
    }

    clock = clock + 1;
	}

  // ******************************
  //             REPORT
  // ******************************
  bool check_result = true;
  bool check_ninst = true;
  bool check_trigger = true;

  cout << endl;

  // ------------------------------
  //              TEST
  // ------------------------------
  if (use_test) {
    check_result = (result == 0);
    check_ninst = !use_ninst || ((instret >= ninst) && (instret < ninst + NCORECOMMIT));
    check_trigger = !use_trigger || (cycle == ntrigger);

    if (check_result && check_trigger && check_ninst) {
      cout << "\033[1;32m";
      cout << "TEST REPORT: SUCCESS." << endl;
      cout << "\033[0m";
    } else if (check_result) {
      cout << "\033[1;33m";
      cout << "TEST REPORT: WRONG INFOS." << endl;
      cout << "\033[0m";
    } else if (!use_trigger || (clock >= (ntrigger + TRIGGER_DELAY))) {
      cout << "\033[1;31m";
      cout << "TEST REPORT: TIMEOUT." << endl;
      cout << "\033[0m";
    } else {
      cout << "\033[1;31m";
      cout << "TEST REPORT: FAILED." << endl;
      cout << "\033[0m";
    }    

    if (!check_ninst) {
      cout << "Expected instructions: " << ninst << endl;
    }
    cout << "Retired instructions: " << instret << endl;
    if (!check_trigger) {
      cout << "Expected cycles: " << ntrigger << endl;
    }
    cout << "Real cycles: " << cycle << endl;   
  // ------------------------------
  //              SIM
  // ------------------------------
  } else {
    //cout << "\033[1;37m";
    //cout << "\033[0m";    
  }

  // ------------------------------
  //            COMMON
  // ------------------------------
  //cout << "\033[1;37m";
  cout << "BOOT file: " << bootfile << endl;
  if (use_rom) {
    cout << "ROM file: " << romfile << endl;
  }
  if (use_vcd) {
    cout << "VCD file: " << vcdfile << endl;
  }
  cout << "Simulation clock cycles: " << clock << endl;  
  //cout << "\033[0m"; 

  // ******************************
  //             CLOSE
  // ******************************
  if (use_etd) {
    etd_close_trace();
  }

  dut_trace->close();
  exit(EXIT_SUCCESS);
}

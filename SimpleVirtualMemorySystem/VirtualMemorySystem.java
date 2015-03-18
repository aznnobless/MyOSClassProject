public class VirtualMemorySystem {

    public final static int FRAME_SIZE = 512;
    public final static int SEGMENT_TABLE_SIZE = FRAME_SIZE;

    private PhysicalMemory pm;
    private TLB tlb;

    // Default Constructor
    public VirtualMemorySystem() {

        pm = PhysicalMemory.getPhysicalMemory();
        tlb = new TLB();

    }

    // Translate VA without utilizing TLB
    public void translateVirtualAddress(int opCode, int rawVirtualAddress, boolean hasTLB) {

        int s = extractSegmentInfoFromVirtualAddress(rawVirtualAddress); // extract index of segment table
        int p = extractPageInfoFromVirtualAddress(rawVirtualAddress);  // extract index of page table
        int f = extractOffsetFromVirtualAddress(rawVirtualAddress); // extract offset of page

        VirtualMemory vm = createVirutualMemory(p, s, f); // generate a virutal address

        // if opcode is read
        if(opCode == 0) {
            if(hasTLB)
                readOperationUsingTLB( vm );
            else
                readOpeartion( vm );
        }

        // if op code is write
        if(opCode == 1) {
            if(hasTLB)
                writeOperationUsingTLB( vm );
            else
                writeOperation( vm );

        }
        // Below 2 lines are debug purpose
        if(!hasTLB) {
            System.out.println("s : " + s + " p : " + p + " f : " + f);
            System.out.println("VirtualAddress : " + vm.getVirtualAddress());
        }

    }

    // read operation of TLB utilizing mode
    public void readOperationUsingTLB(VirtualMemory vm) {

        TLBEntry entry = new TLBEntry(vm);
        int w = vm.getOffsetWithPage();

        int tlbLookUp = tlb.search(entry);

        // HIT!!
        if(tlbLookUp != -1) {

            Utility.getInstance().appendToTlbOutput("h");
            // System.out.println("hit @ readOperationUsingTLB");

            int f = tlb.getTLBEntryFromBuffer(tlbLookUp).getStartFrameAddress();
            int physicalAddress = f + w;

            Utility.getInstance().appendToTlbOutput( Integer.toString(physicalAddress) );
            //System.out.println(physicalAddress);

            entry.setReplacementInformation(3);
            entry.setStartFrameAddress(f);

            // update
            tlb.add(entry, true);
        }
        // MISS!!
        else {
            // TLB doesn't have information of this virtual vm, so need to access Physical Memory

            // STEP 1 : Retrieve stEntry index and stEtry index is valid or not.
            int stEntry = pm.getValueAtIndex( vm.getSegmentNumber() );
            if(stEntry == -1) {
                Utility.getInstance().appendToTlbOutput("pf");
                return;
            } else if(stEntry == 0) {
                Utility.getInstance().appendToTlbOutput("err");
                return;
            }

            // STEP 2 : Retrieve ptEntry index and ptEntry index is valid or not
            int ptEntry = pm.getValueAtIndex( stEntry + vm.getPageNumber());
            if(ptEntry == -1) {
                Utility.getInstance().appendToTlbOutput("pf");
                return;
            }
            else if(ptEntry == 0) {
                Utility.getInstance().appendToTlbOutput("err");
                return;
            }
            else {
                int physicalAddress = ptEntry + vm.getOffsetWithPage();
                Utility.getInstance().appendToTlbOutput("m");
                //System.out.println("miss");

                entry.setReplacementInformation(3);
                entry.setStartFrameAddress(ptEntry);

                tlb.add(entry, false);

                Utility.getInstance().appendToTlbOutput(Integer.toString(physicalAddress));
                //System.out.println(physicalAddress);

            }

        }

    }

    // write operation of TLB utilizing mode
    public void writeOperationUsingTLB(VirtualMemory vm) {

        TLBEntry entry = new TLBEntry(vm);
        int w = vm.getOffsetWithPage();

        int tlbLookUp = tlb.search(entry);

        //HIT!!
        if(tlbLookUp != -1) {
            Utility.getInstance().appendToTlbOutput("h");
            //System.out.println("Hit!!");

            int f = tlb.getTLBEntryFromBuffer(tlbLookUp).getStartFrameAddress();
            int physicalAddress = f + w;

            Utility.getInstance().appendToTlbOutput(Integer.toString(physicalAddress));
            //System.out.println(physicalAddress);

            entry.setReplacementInformation(3);
            entry.setStartFrameAddress(f);

            // update tlb
            tlb.add(entry, true);

        }
        // MISS
        else {
            // TLB doesn't have information of this virtual vm, so need to access Physical Memory

            // STEP 1 : Retrieve stEntry index and stEtry index is valid or not.
            int stEntry = pm.getValueAtIndex( vm.getSegmentNumber() );
            if(stEntry == -1) {
                Utility.getInstance().appendToTlbOutput("pf");
                return;
            } else if(stEntry == 0) {
                Utility.getInstance().appendToTlbOutput("m&&&&");
                stEntry = createNewPageTable_TLB_Mode(vm);
                int ptEntry = pm.getValueAtIndex(stEntry+ vm.getPageNumber());
                entry.setReplacementInformation(3);
                entry.setStartFrameAddress(ptEntry);

                tlb.add(entry, false);

                return;
            }

            // STEP 2 : Retrieve ptEntry index and ptEntry index is valid or not
            int ptEntry = pm.getValueAtIndex( stEntry + vm.getPageNumber());
            if(ptEntry == -1) {
                Utility.getInstance().appendToTlbOutput("pf");
                return;
            }
            else if(ptEntry == 0) {
                Utility.getInstance().appendToTlbOutput("m");
                ptEntry = createNewPage_TLB_Mode(vm);
                int physicalAddress = ptEntry + vm.getOffsetWithPage();
                entry.setReplacementInformation(3);
                entry.setStartFrameAddress(ptEntry);

                tlb.add(entry, false);
                return;
            }


            else {
                int physicalAddress = ptEntry + vm.getOffsetWithPage();
                if(physicalAddress == -1) {
                    Utility.getInstance().appendToTlbOutput("pf");
                    return;
                    //System.out.println("pf ");
                }else {
                    Utility.getInstance().appendToTlbOutput("m");
                    Utility.getInstance().appendToTlbOutput(Integer.toString(physicalAddress));
                    //System.out.println(physicalAddress);

                    entry.setReplacementInformation(3);
                    entry.setStartFrameAddress(ptEntry);

                    tlb.add(entry, false);

                    tlb.showTLB();
                }

            }

        }

    }

    public void readOpeartion(VirtualMemory vm) {

        // STEP 1 : Retrieve stEntry and check stEntry is valid or not
        int stEntry = pm.getSegementTableEntry(vm.getSegmentNumber());

        if( stEntry == -1)  {
            Utility.getInstance().appendToWithoutTlbOutput("pf");
            return;
            //System.out.print("pf ");
        } else if( stEntry == 0) {
            Utility.getInstance().appendToWithoutTlbOutput("err");
            return;
        }

        // STEP 2 : Retreive ptEntry and check ptEntry is valid or not
        int ptEntry = pm.getPageTableEntry( stEntry + vm.getPageNumber());

        if( ptEntry == -1) {
            Utility.getInstance().appendToWithoutTlbOutput("pf");
            return;
        } else if( ptEntry == 0) {
            Utility.getInstance().appendToWithoutTlbOutput("err");
            return;
        }
        else {
            int physicalAddress = ptEntry + vm.getOffsetWithPage();
            if(physicalAddress == -1) {
                Utility.getInstance().appendToWithoutTlbOutput("pf");
                //System.out.print("pf ");
            }else {
                Utility.getInstance().appendToWithoutTlbOutput(Integer.toString(physicalAddress));
                //System.out.print(physicalAddress + " ");
            }
        }

    }

    public void writeOperation(VirtualMemory vm) {

        // STEP 1 : retrieve stEntry and check it's validity. If stEntry == -1, return
        int stEntry = pm.getValueAtIndex( vm.getSegmentNumber() );

        System.out.println("에스티 : " +  stEntry);

        if( (stEntry == -1) ) {
            Utility.getInstance().appendToWithoutTlbOutput("pf");
            //System.out.print("pf ");
            return;

        } else if(stEntry == 0) {
            // !!!CHECKPOINT!!! : Watch out! when memory is full, this function doesn't work properly.
            System.out.println("여기!");
            stEntry = createNewPageTable(vm);

            System.out.println("여기!! " + stEntry);
            return;

        }

        // STEP 2 : retrieve ptEntry and check it's validity. If ptEntry == -1, return
        int ptEntry = pm.getValueAtIndex( stEntry + vm.getPageNumber());

        if( (ptEntry == -1) ) {
            Utility.getInstance().appendToWithoutTlbOutput("pf");
            //System.out.print("pf ");
            return;
        }
        else if( (ptEntry == 0) ) {
            //System.out.println("Create new blank page, and continue with translation process");
            ptEntry = createNewPage(vm);
        }

        else {

           int physicalAddress = ptEntry+vm.getOffsetWithPage();
           if(physicalAddress == -1) {
               Utility.getInstance().appendToWithoutTlbOutput("pf");
               //System.out.print("pf ");
           }else {
               Utility.getInstance().appendToWithoutTlbOutput(Integer.toString(physicalAddress));
               //System.out.print(physicalAddress + " ");
           }

        }

    }

    public int createNewPageTable_TLB_Mode(VirtualMemory vm) {

        int result = 0;

        // if segment is empty
        if( !pm.isSegmentEntryIndexEmpty(vm.getSegmentNumber()) ) {

            // Step 1: Using bitmap, find first empty frame (make sure that two consecutive frames are empty

            int candidateFrameNumber = pm.searchFirstFreeFrameUsingBitmap(0);
            boolean isFound = false;
            while (!isFound) {

                isFound = pm.isTwoConsecutiveFrameEmpty(candidateFrameNumber);

                //System.out.println( candidateFrameNumber +  "  !!  " + isFound);

                if(!isFound) {

                    candidateFrameNumber = pm.searchFirstFreeFrameUsingBitmap(candidateFrameNumber+1);
                    if(candidateFrameNumber == 1023 || candidateFrameNumber == -1) {
                        System.err.println("No available frame");
                        return result;

                    }
                    //System.out.println("candidateNumber : " + candidateFrameNumber);
                }

            }

            if(isFound) {
                // Step 2: change frame number to actual physical address and assign it to STentry
                int actualStartingPhysicalAddress = candidateFrameNumber * PhysicalMemory.FRAME_SIZE;
                pm.updateSegmentTableEntry(vm.getSegmentNumber(), actualStartingPhysicalAddress);

//                // Step 3: Update bitmap
//                pm.updateTargetFrameBitmapFlagUp(vm.getSegmentNumber());

                // Step 4: Create new page for this
                result = createNewPage_TLB_Mode(vm);


            } else {
                System.err.println("Error @ VirtualMemorySystem.writeOperation() @segment"
                        +"Couldn't complete the job due to no available frame");
            }

        }

        return result;
    }

    public int createNewPageTable(VirtualMemory vm) {

        System.out.println("불려짐 세그먼트넘버 " + vm.getSegmentNumber());
        System.out.println(pm.isSegmentEntryIndexEmpty(vm.getSegmentNumber()) );

        int actualStartingPhysicalAddress = 0;

        // if segment is empty
        if( pm.isSegmentEntryIndexEmpty(vm.getSegmentNumber()) ) {

            System.out.println("도달함");

            // Step 1: Using bitmap, find first empty frame (make sure that two consecutive frames are empty

            int candidateFrameNumber = pm.searchFirstFreeFrameUsingBitmap(0);

            boolean isFound = false;
            while (!isFound) {

                isFound = pm.isTwoConsecutiveFrameEmpty(candidateFrameNumber);

                //System.out.println( candidateFrameNumber +  "  !!  " + isFound);

                if(!isFound) {

                    candidateFrameNumber = pm.searchFirstFreeFrameUsingBitmap(candidateFrameNumber+1);
                    if(candidateFrameNumber == 1023 || candidateFrameNumber == -1) {
                        System.err.println("No available frame");
                        break;
                    }
                    //System.out.println("candidateNumber : " + candidateFrameNumber);
                }

            }

            if(isFound) {
                // Step 2: change frame number to actual physical address and assign it to STentry
                actualStartingPhysicalAddress = candidateFrameNumber * PhysicalMemory.FRAME_SIZE;
                pm.updateSegmentTableEntry(vm.getSegmentNumber(), actualStartingPhysicalAddress);

                // Step 3: Update bitmap (DONT UPDATE BITMAP updateSegmentTableEntry already update the bitmap)
                //pm.updateTargetFrameBitmapFlagUp(vm.getSegmentNumber());
//
//                System.out.println("------ Bitmap Information ------");
//                showBitmapInPhysicalMemory();

                //Utility.getInstance().appendToWithoutTlbOutput("안녕");

                // Step 4: Create new page for this
                createNewPage(vm);


            } else {
                System.err.println("Error @ VirtualMemorySystem.writeOperation() @segment"
                        +"Couldn't complete the job due to no available frame");
            }

        }

        return actualStartingPhysicalAddress;
    }


    public int createNewPage(VirtualMemory vm) {

        System.out.println("실행됨");

        int physicalAddress = 0;
        int candidateFrameNumber = pm.searchFirstFreeFrameUsingBitmap(0);

        boolean isFound = false;
        while(!isFound) {

            isFound = pm.isTargetFrameEmpty(candidateFrameNumber);

            if(!isFound) {
                candidateFrameNumber = pm.searchFirstFreeFrameUsingBitmap(candidateFrameNumber+1);
                if(candidateFrameNumber == 1024) {
                    System.err.println("Error @ VirtualMemorySystem.writeOperation()" +
                            "@ page : Couldn't complete the job due to no avaaile frame");
                    break;
                }
            }

            if(isFound) {

                System.out.println("페이지를만들어요");

                int actualStartingPhysicalAddress = candidateFrameNumber * PhysicalMemory.FRAME_SIZE;
                pm.updatePageTableEntryToPhysicalMemory(
                        vm.getPageNumber(), vm.getSegmentNumber(), actualStartingPhysicalAddress);

                int stEntry = pm.getValueAtIndex( vm.getSegmentNumber() );
                int ptEntry = pm.getValueAtIndex( stEntry + vm.getPageNumber());
                physicalAddress = ptEntry + vm.getOffsetWithPage();

                if(physicalAddress == -1) {
                    Utility.getInstance().appendToWithoutTlbOutput("pf");
                    //System.out.print("pf ");
                }
                else {

                    Utility.getInstance().appendToWithoutTlbOutput(Integer.toString(physicalAddress));
                    //System.out.print(physicalAddress + " ");
                }
            }
        }
        return physicalAddress;
    }


    public int createNewPage_TLB_Mode(VirtualMemory vm) {

        int candidateFrameNumber = pm.searchFirstFreeFrameUsingBitmap(0);
        boolean isFound = false;
        int result = -1;

        while(!isFound) {

            isFound = pm.isTargetFrameEmpty(candidateFrameNumber);


            if(!isFound) {
                candidateFrameNumber = pm.searchFirstFreeFrameUsingBitmap(candidateFrameNumber+1);
                if(candidateFrameNumber == 1024) {
                    System.err.println("Error @ VirtualMemorySystem.writeOperation()" +
                            "@ page : Couldn't complete the job due to no avaaile frame");
                    return result;
                }
            }

            if(isFound) {
                int actualStartingPhysicalAddress = candidateFrameNumber * PhysicalMemory.FRAME_SIZE;
                pm.updatePageTableEntryToPhysicalMemory(
                        vm.getPageNumber(), vm.getSegmentNumber(), actualStartingPhysicalAddress);

                int stEntry = pm.getValueAtIndex( vm.getSegmentNumber() );
                int ptEntry = pm.getValueAtIndex( stEntry + vm.getPageNumber());
                int physicalAddress = ptEntry + vm.getOffsetWithPage();

                if(physicalAddress == -1) {
                    Utility.getInstance().appendToWithoutTlbOutput("pf");
                    //System.out.print("pf ");
                }else {
                    Utility.getInstance().appendToTlbOutput(Integer.toString(physicalAddress));
                    //System.out.print(physicalAddress + " ");
                    result = physicalAddress;
                }
            }
        }

        return result;

    }


    public VirtualMemory createVirutualMemory(int pageNumber, int segmentNumber,  int offset) {

        VirtualMemory vm = new VirtualMemory(segmentNumber, pageNumber, offset);

        return vm;

    }


    // Physical Memory Controller part
    public void updateSegmentTableEntryToPhysicalMemory(int index, int startingAddressOfPageTable) {

        if(index < 0 || index >= SEGMENT_TABLE_SIZE) {
            System.err.println("Error @ VirtualMemorySystem.updateSegmentTableEntryToPhysicalMemory() : invalid index");
            return ;
        }

        pm.updateSegmentTableEntry(index, startingAddressOfPageTable);

    }

    public void updatePageTableEntryToPhysicalMemory(int pageIndex, int segmentEntryIndex, int startingAddressOfPage) {

        // Check valid segmentEntry index : valid range 0 ~ 511
        if(segmentEntryIndex < 0 || segmentEntryIndex >= SEGMENT_TABLE_SIZE) {
            System.err.println(
                    "Error @ VirtualMemorySystem.updatePageTableEntryToPhysicalMemory() : invalid segmentEntryIndex");
            return;
        }

        // Check valid pageIndex : valid pageIndex 0 ~ 1023
        if(pageIndex < 0 || pageIndex >= PhysicalMemory.PAGE_TABLE_SIZE) {
            System.err.println(
                    "Error @ VirualMemorySystem.updatePageTableEntryToPhysicalMemory() : invalid pageIndex");
            return;
        }

        pm.updatePageTableEntryToPhysicalMemory(pageIndex, segmentEntryIndex, startingAddressOfPage);

    }

//    private int frameNumberToPhysicalAddress(int frameNumber) {
//        return frameNumber * PhysicalMemory.FRAME_SIZE;
//    }
//
    public void showSegmentTableInPhysicalMemory() {
        pm.showSegmentTableInPhysicalMemory();
    }

    public void showBitmapInPhysicalMemory() { pm.showBitmap(); }


    // End of Physical Memory Controller part


    // Virtual Memory Contoler part

    private int extractSegmentInfoFromVirtualAddress(int virtualAddress) {
        int temp = virtualAddress;

        return (temp & 0xFF80000) >> 19;
    }

    private int extractPageInfoFromVirtualAddress(int virtualAddress) {
        int temp= virtualAddress;

        return (0 | ((virtualAddress) & 0x7FE00)) >> 9;
    }

    private int extractOffsetFromVirtualAddress(int virtualAddress) {
        int temp = virtualAddress;

        return (temp & 0x1ff);
    }
    // End of Virtual Memory Controller part

}

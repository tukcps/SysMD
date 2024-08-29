## Variable that points to SystemC installation path
SYSTEMC = $(SYSTEMC_PATH)

## Variable that points to SystemC-AMS installation path
SYSTEMCAMS = $(SYSTEMC_AMS_PATH)

ifndef TARGET_ARCH
## select target architecture
#TARGET_ARCH  = linux
TARGET_ARCH = linux64
#TARGET_ARCH = gccsparcOS5
#TARGET_ARCH = cygwin
#TARGET_ARCH = mingw32
endif

#Compiler Options:
CC = g++

INCDIR = -I. -I$(SYSTEMC)/include -I$(SYSTEMCAMS)/include
LIBDIR = -L$(SYSTEMC)/lib-$(TARGET_ARCH) -L$(SYSTEMCAMS)/lib-$(TARGET_ARCH)
LIBS   =   $(EXTRA_LIBS) -lsystemc-ams -lsystemc -lm

final.exe: modules/transmitter_CLASS.o modules/wirelessDevice_CLASS.o modules/receiver_CLASS.o main.o 
		$(CC) $(INCDIR) $(LIBDIR) modules/transmitter_CLASS.o modules/wirelessDevice_CLASS.o modules/receiver_CLASS.o main.o -o final.exe $(LIBS) 2>&1 | c++filt

main.o: main.cpp
		 $(CC) $(INCDIR) -c main.cpp -o main.o

modules/transmitter_CLASS.o: modules/transmitter_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/transmitter_CLASS.cpp -o modules/transmitter_CLASS.o

modules/wirelessDevice_CLASS.o: modules/wirelessDevice_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/wirelessDevice_CLASS.cpp -o modules/wirelessDevice_CLASS.o

modules/receiver_CLASS.o: modules/receiver_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/receiver_CLASS.cpp -o modules/receiver_CLASS.o

clean:
	rm -f *.o final.exe
	rm -f modules/*.o
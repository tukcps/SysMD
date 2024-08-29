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

final.exe: modules/B_CLASS.o modules/C_CLASS.o modules/Y_CLASS.o modules/A_CLASS.o modules/X_CLASS.o modules/D_CLASS.o main.o 
		$(CC) $(INCDIR) $(LIBDIR) modules/B_CLASS.o modules/C_CLASS.o modules/Y_CLASS.o modules/A_CLASS.o modules/X_CLASS.o modules/D_CLASS.o main.o -o final.exe $(LIBS) 2>&1 | c++filt

main.o: main.cpp
		 $(CC) $(INCDIR) -c main.cpp -o main.o

modules/B_CLASS.o: modules/B_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/B_CLASS.cpp -o modules/B_CLASS.o

modules/C_CLASS.o: modules/C_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/C_CLASS.cpp -o modules/C_CLASS.o

modules/Y_CLASS.o: modules/Y_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/Y_CLASS.cpp -o modules/Y_CLASS.o

modules/A_CLASS.o: modules/A_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/A_CLASS.cpp -o modules/A_CLASS.o

modules/X_CLASS.o: modules/X_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/X_CLASS.cpp -o modules/X_CLASS.o

modules/D_CLASS.o: modules/D_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/D_CLASS.cpp -o modules/D_CLASS.o

clean:
	rm -f *.o final.exe
	rm -f modules/*.o
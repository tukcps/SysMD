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

final.exe: modules/testModuleB_CLASS.o modules/someA_CLASS.o modules/someB_CLASS.o modules/compA.o modules/compB.o modules/testModuleA_CLASS.o main.o 
		$(CC) $(INCDIR) $(LIBDIR) modules/testModuleB_CLASS.o modules/someA_CLASS.o modules/someB_CLASS.o modules/compA.o modules/compB.o modules/testModuleA_CLASS.o main.o -o final.exe $(LIBS) 2>&1 | c++filt

main.o: main.cpp
		 $(CC) $(INCDIR) -c main.cpp -o main.o

modules/testModuleB_CLASS.o: modules/testModuleB_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/testModuleB_CLASS.cpp -o modules/testModuleB_CLASS.o

modules/someA_CLASS.o: modules/someA_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/someA_CLASS.cpp -o modules/someA_CLASS.o

modules/someB_CLASS.o: modules/someB_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/someB_CLASS.cpp -o modules/someB_CLASS.o

modules/compA.o: modules/compA.cpp
		 $(CC) $(INCDIR) -c modules/compA.cpp -o modules/compA.o

modules/compB.o: modules/compB.cpp
		 $(CC) $(INCDIR) -c modules/compB.cpp -o modules/compB.o

modules/testModuleA_CLASS.o: modules/testModuleA_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/testModuleA_CLASS.cpp -o modules/testModuleA_CLASS.o

clean:
	rm -f *.o final.exe
	rm -f modules/*.o
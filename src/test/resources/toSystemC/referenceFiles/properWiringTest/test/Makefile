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

final.exe: modules/ClassA.o modules/actualPartA_CLASS.o modules/PartA_CLASS.o modules/actualPartB_CLASS.o modules/ClassB.o modules/DefinedPartA.o modules/DefinedPartB.o modules/PartB_TrueInheritance_CLASS.o modules/PartB_CLASS.o modules/actualPartD_CLASS.o modules/PartD_CLASS.o modules/PartC_CLASS.o modules/PartA_TrueInheritance_CLASS.o modules/actualPartC_CLASS.o main.o 
		$(CC) $(INCDIR) $(LIBDIR) modules/ClassA.o modules/actualPartA_CLASS.o modules/PartA_CLASS.o modules/actualPartB_CLASS.o modules/ClassB.o modules/DefinedPartA.o modules/DefinedPartB.o modules/PartB_TrueInheritance_CLASS.o modules/PartB_CLASS.o modules/actualPartD_CLASS.o modules/PartD_CLASS.o modules/PartC_CLASS.o modules/PartA_TrueInheritance_CLASS.o modules/actualPartC_CLASS.o main.o -o final.exe $(LIBS) 2>&1 | c++filt

main.o: main.cpp
		 $(CC) $(INCDIR) -c main.cpp -o main.o

modules/ClassA.o: modules/ClassA.cpp
		 $(CC) $(INCDIR) -c modules/ClassA.cpp -o modules/ClassA.o

modules/actualPartA_CLASS.o: modules/actualPartA_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/actualPartA_CLASS.cpp -o modules/actualPartA_CLASS.o

modules/PartA_CLASS.o: modules/PartA_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/PartA_CLASS.cpp -o modules/PartA_CLASS.o

modules/actualPartB_CLASS.o: modules/actualPartB_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/actualPartB_CLASS.cpp -o modules/actualPartB_CLASS.o

modules/ClassB.o: modules/ClassB.cpp
		 $(CC) $(INCDIR) -c modules/ClassB.cpp -o modules/ClassB.o

modules/DefinedPartA.o: modules/DefinedPartA.cpp
		 $(CC) $(INCDIR) -c modules/DefinedPartA.cpp -o modules/DefinedPartA.o

modules/DefinedPartB.o: modules/DefinedPartB.cpp
		 $(CC) $(INCDIR) -c modules/DefinedPartB.cpp -o modules/DefinedPartB.o

modules/PartB_TrueInheritance_CLASS.o: modules/PartB_TrueInheritance_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/PartB_TrueInheritance_CLASS.cpp -o modules/PartB_TrueInheritance_CLASS.o

modules/PartB_CLASS.o: modules/PartB_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/PartB_CLASS.cpp -o modules/PartB_CLASS.o

modules/actualPartD_CLASS.o: modules/actualPartD_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/actualPartD_CLASS.cpp -o modules/actualPartD_CLASS.o

modules/PartD_CLASS.o: modules/PartD_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/PartD_CLASS.cpp -o modules/PartD_CLASS.o

modules/PartC_CLASS.o: modules/PartC_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/PartC_CLASS.cpp -o modules/PartC_CLASS.o

modules/PartA_TrueInheritance_CLASS.o: modules/PartA_TrueInheritance_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/PartA_TrueInheritance_CLASS.cpp -o modules/PartA_TrueInheritance_CLASS.o

modules/actualPartC_CLASS.o: modules/actualPartC_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/actualPartC_CLASS.cpp -o modules/actualPartC_CLASS.o

clean:
	rm -f *.o final.exe
	rm -f modules/*.o
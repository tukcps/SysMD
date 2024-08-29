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

final.exe: modules/y_CLASS.o modules/c_CLASS.o modules/a_CLASS.o modules/XYZ.o modules/x_CLASS.o modules/b_CLASS.o main.o 
		$(CC) $(INCDIR) $(LIBDIR) modules/y_CLASS.o modules/c_CLASS.o modules/a_CLASS.o modules/XYZ.o modules/x_CLASS.o modules/b_CLASS.o main.o -o final.exe $(LIBS) 2>&1 | c++filt

main.o: main.cpp
		 $(CC) $(INCDIR) -c main.cpp -o main.o

modules/y_CLASS.o: modules/y_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/y_CLASS.cpp -o modules/y_CLASS.o

modules/c_CLASS.o: modules/c_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/c_CLASS.cpp -o modules/c_CLASS.o

modules/a_CLASS.o: modules/a_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/a_CLASS.cpp -o modules/a_CLASS.o

modules/XYZ.o: modules/XYZ.cpp
		 $(CC) $(INCDIR) -c modules/XYZ.cpp -o modules/XYZ.o

modules/x_CLASS.o: modules/x_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/x_CLASS.cpp -o modules/x_CLASS.o

modules/b_CLASS.o: modules/b_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/b_CLASS.cpp -o modules/b_CLASS.o

clean:
	rm -f *.o final.exe
	rm -f modules/*.o
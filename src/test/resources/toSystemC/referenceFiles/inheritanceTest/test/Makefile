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

final.exe: modules/subClassWithOverride_CLASS.o modules/SubClassWithNewAttribute.o modules/subClassOnlyInherit_CLASS.o modules/SubClassWithOverride.o modules/subClassWithNewAttribute_CLASS.o modules/SuperClass.o modules/overrideSuperAttribute_CLASS.o modules/justInstantiateSuperClass_CLASS.o modules/SubClassOnlyInherit.o modules/addNewAttribute_CLASS.o main.o 
		$(CC) $(INCDIR) $(LIBDIR) modules/subClassWithOverride_CLASS.o modules/SubClassWithNewAttribute.o modules/subClassOnlyInherit_CLASS.o modules/SubClassWithOverride.o modules/subClassWithNewAttribute_CLASS.o modules/SuperClass.o modules/overrideSuperAttribute_CLASS.o modules/justInstantiateSuperClass_CLASS.o modules/SubClassOnlyInherit.o modules/addNewAttribute_CLASS.o main.o -o final.exe $(LIBS) 2>&1 | c++filt

main.o: main.cpp
		 $(CC) $(INCDIR) -c main.cpp -o main.o

modules/subClassWithOverride_CLASS.o: modules/subClassWithOverride_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/subClassWithOverride_CLASS.cpp -o modules/subClassWithOverride_CLASS.o

modules/SubClassWithNewAttribute.o: modules/SubClassWithNewAttribute.cpp
		 $(CC) $(INCDIR) -c modules/SubClassWithNewAttribute.cpp -o modules/SubClassWithNewAttribute.o

modules/subClassOnlyInherit_CLASS.o: modules/subClassOnlyInherit_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/subClassOnlyInherit_CLASS.cpp -o modules/subClassOnlyInherit_CLASS.o

modules/SubClassWithOverride.o: modules/SubClassWithOverride.cpp
		 $(CC) $(INCDIR) -c modules/SubClassWithOverride.cpp -o modules/SubClassWithOverride.o

modules/subClassWithNewAttribute_CLASS.o: modules/subClassWithNewAttribute_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/subClassWithNewAttribute_CLASS.cpp -o modules/subClassWithNewAttribute_CLASS.o

modules/SuperClass.o: modules/SuperClass.cpp
		 $(CC) $(INCDIR) -c modules/SuperClass.cpp -o modules/SuperClass.o

modules/overrideSuperAttribute_CLASS.o: modules/overrideSuperAttribute_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/overrideSuperAttribute_CLASS.cpp -o modules/overrideSuperAttribute_CLASS.o

modules/justInstantiateSuperClass_CLASS.o: modules/justInstantiateSuperClass_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/justInstantiateSuperClass_CLASS.cpp -o modules/justInstantiateSuperClass_CLASS.o

modules/SubClassOnlyInherit.o: modules/SubClassOnlyInherit.cpp
		 $(CC) $(INCDIR) -c modules/SubClassOnlyInherit.cpp -o modules/SubClassOnlyInherit.o

modules/addNewAttribute_CLASS.o: modules/addNewAttribute_CLASS.cpp
		 $(CC) $(INCDIR) -c modules/addNewAttribute_CLASS.cpp -o modules/addNewAttribute_CLASS.o

clean:
	rm -f *.o final.exe
	rm -f modules/*.o
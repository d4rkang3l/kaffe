/*
 * errors.h
 * Error return codes.
 *
 * Copyright (c) 1996, 1997
 *	Transvirtual Technologies, Inc.  All rights reserved.
 *
 * See the file "license.terms" for information on usage and redistribution 
 * of this file. 
 */

#ifndef __errors_h
#define __errors_h

#include <stdarg.h>

#define KERR_EXCEPTION			0x0001
#define KERR_RETHROW			0x0002
#define KERR_INITIALIZER_ERROR		0x0004
#define KERR_OUT_OF_MEMORY		0x0008
#define KERR_CODE_MASK			0x00ff

#define KERR_FREE_MESSAGE		0x8000

struct Hjava_lang_Throwable;

/*
 * This struct serves to keep information about an exception
 * that will be thrown later if some operation failed.
 */
typedef struct _errorInfo {
	int type;		/* type of error */
	const char* classname;	/* full dotted name of exception/error class */
	char* mess;		/* accompanying msg */
	struct Hjava_lang_Throwable* throwable;	/* a throwable */
} errorInfo;

/* post a short exception without message */
extern void postException(errorInfo *, const char *name);

/* post an exception with a print like message */
extern void postExceptionMessage(errorInfo *, 
	const char *name, const char *msgfmt, ...);

/* va_list version of postExceptionMessage */
extern void vpostExceptionMessage(errorInfo *einfo,
        const char * fullname, const char * fmt, va_list args);

/* post an out of memory condition */
extern void postOutOfMemory(errorInfo *einfo);

#define MAX_ERROR_MESSAGE_SIZE        1024

extern void discardErrorInfo(errorInfo *);
/* dump error info to stderr */
extern void dumpErrorInfo(errorInfo *);

#define JAVA_LANG(NAME)		"java.lang." #NAME
#define JAVA_IO(NAME)		"java.io." #NAME


#define NEW_LANG_EXCEPTION(NAME) \
  (struct Hjava_lang_Throwable*)execute_java_constructor("java.lang." #NAME, \
	0, "()V")

#define NEW_LANG_EXCEPTION_MESSAGE(NAME, MESS) \
  (struct Hjava_lang_Throwable*)execute_java_constructor("java.lang." #NAME, \
	0, "(Ljava/lang/String;)V", stringC2Java(MESS))

#define NEW_IO_EXCEPTION(NAME) \
  (struct Hjava_lang_Throwable*)execute_java_constructor("java.io." #NAME, \
	0, "()V")

#define NEW_IO_EXCEPTION_MESSAGE(NAME, MESS) \
  (struct Hjava_lang_Throwable*)execute_java_constructor("java.io." #NAME, \
	0, "(Ljava/lang/String;)V", stringC2Java(MESS))

#define NoSuchMethodError(M) NEW_LANG_EXCEPTION_MESSAGE(NoSuchMethodError, M)
#define LinkageError NEW_LANG_EXCEPTION(LinkageError)
#define OutOfMemoryError NEW_LANG_EXCEPTION(OutOfMemoryError)
#define UnsatisfiedLinkError NEW_LANG_EXCEPTION(UnsatisfiedLinkError)
#define VirtualMachineError NEW_LANG_EXCEPTION(VirtualMachineError)
#define IncompatibleClassChangeError NEW_LANG_EXCEPTION(IncompatibleClassChangeError)
#define IllegalAccessError NEW_LANG_EXCEPTION(IllegalAccessError)
#define NegativeArraySizeException NEW_LANG_EXCEPTION(NegativeArraySizeException)
#define ClassCastException(M) NEW_LANG_EXCEPTION_MESSAGE(ClassCastException, M)
#define IllegalMonitorStateException NEW_LANG_EXCEPTION(IllegalMonitorStateException)
#define NullPointerException NEW_LANG_EXCEPTION(NullPointerException)
#define ArrayIndexOutOfBoundsException NEW_LANG_EXCEPTION(ArrayIndexOutOfBoundsException)
#define ArrayStoreException NEW_LANG_EXCEPTION(ArrayStoreException)
#define ArithmeticException NEW_LANG_EXCEPTION(ArithmeticException)
#define AbstractMethodError NEW_LANG_EXCEPTION(AbstractMethodError)
#define ThreadDeath NEW_LANG_EXCEPTION(ThreadDeath)
#define StackOverflowError NEW_LANG_EXCEPTION(StackOverflowError)
#define IllegalThreadStateException NEW_LANG_EXCEPTION(IllegalThreadStateException)
#define	InstantiationException(M) NEW_LANG_EXCEPTION_MESSAGE(InstantiationException, M)

void throwError(struct _errorInfo*) __NORETURN__;

/*
 * KMALLOC and all the allocating string functions return null on
 * error.  This convenience function can be used when it is safe to
 * signal the error immidiately.
 */
static inline 
void *checkPtr(void *p)
{
	if (!p) {
		errorInfo info;
		postOutOfMemory(&info);
		throwError(&info);
	}
	return p;
}

#endif

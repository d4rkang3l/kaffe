/* ObjectStreamField.java -- Class used to store name and class of fields
   Copyright (C) 1998, 1999, 2003 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.
 
GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package java.io;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import gnu.java.lang.reflect.TypeSignature;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * This class intends to describe the field of a class for the serialization
 * subsystem. Serializable fields in a serializable class can be explicitly
 * exported using an array of ObjectStreamFields.
 */
public class ObjectStreamField implements Comparable
{
  private String name;
  private Class type;
  private String typename;
  private int offset = -1; // XXX make sure this is correct
  private boolean unshared;
  private boolean persistent = false;
  private boolean toset = true;
  private Field field;

  ObjectStreamField (Field field)
  {
    this (field.getName(), field.getType());
    this.field = field;
    toset = !Modifier.isFinal(field.getModifiers());
  }

  /**
   * This constructor creates an ObjectStreamField instance 
   * which represents a field named <code>name</code> and is
   * of the type <code>type</code>.
   *
   * @param name Name of the field to export.
   * @param type Type of the field in the concerned class.
   */
  public ObjectStreamField (String name, Class type)
  {
    this (name, type, false);
  }

  /**
   * This constructor creates an ObjectStreamField instance 
   * which represents a field named <code>name</code> and is
   * of the type <code>type</code>.
   *
   * @param name Name of the field to export.
   * @param type Type of the field in the concerned class.
   */
  public ObjectStreamField (String name, Class type, boolean unshared)
  {
    if (name == null)
      throw new NullPointerException();

    this.name = name;
    this.type = type;
    this.typename = TypeSignature.getEncodingOfClass(type);
    this.unshared = unshared;
  }
 
  /**
   * There are many cases you can not get java.lang.Class from typename 
   * if your context class loader cannot load it, then use typename to
   * construct the field.
   *
   * @param name Name of the field to export.
   * @param typename The coded name of the type for this field.
   */
  ObjectStreamField (String name, String typename)
  {
    this.name = name;
    this.typename = typename;
    try
      {
        type = TypeSignature.getClassForEncoding(typename);
      }
    catch(ClassNotFoundException e)
      {
      }
  }
  
  /**
   * There are many cases you can not get java.lang.Class from typename 
   * if your context class loader cann not load it, then use typename to
   * construct the field.
   *
   * @param name Name of the field to export.
   * @param typename The coded name of the type for this field.
   * @param loader The class loader to use to resolve class names.
   */
  ObjectStreamField (String name, String typename, ClassLoader loader)
  {
    this.name = name;
    this.typename = typename;
    try
      {
        type = TypeSignature.getClassForEncoding(typename, true, loader);
      }
    catch(ClassNotFoundException e)
      {
      }
  }

  /**
   * This method returns the name of the field represented by the
   * ObjectStreamField instance.
   *
   * @return A string containing the name of the field.
   */
  public String getName ()
  {
    return name;
  }

  /**
   * This method returns the class representing the type of the
   * field which is represented by this instance of ObjectStreamField.
   *
   * @return A class representing the type of the field.
   */
  public Class getType ()
  {
    return type;
  }

  /**
   * This method returns the char encoded type of the field which
   * is represented by this instance of ObjectStreamField.
   *
   * @return A char representing the type of the field.
   */
  public char getTypeCode ()
  {
    return typename.charAt (0);
  }

  /**
   * This method returns a more explicit type name than
   * {@link #getTypeCode()} in the case the type is a real
   * class (and not a primitive).
   *
   * @return The name of the type (class name) if it is not a 
   * primitive, in the other case null is returned.
   */
  public String getTypeString ()
  {
    // use intern()
    if (isPrimitive())
      return null;
    return typename.intern();
  }

  /**
   * This method returns the current offset of the field in
   * the serialization stream relatively to the other fields.
   * The offset is expressed in bytes.
   *
   * @return The offset of the field in bytes.
   * @see #setOffset(int)
   */
  public int getOffset ()
  {
    return offset;
  }

  /**
   * This method sets the current offset of the field.
   * 
   * @param off The offset of the field in bytes.
   * @see getOffset()
   */
  protected void setOffset (int off)
  {
    offset = off;
  }

  /**
   * This method returns whether the field represented by this object is
   * unshared or not.
   *
   * @return Tells if this field is unshared or not.
   */
  public boolean isUnshared ()
  {
    return unshared;
  }

  /**
   * This method returns true if the type of the field
   * represented by this instance is a primitive.
   *
   * @return true if the type is a primitive, false
   * in the other case.
   */
  public boolean isPrimitive ()
  {
    return typename.length() == 1;
  }

  public int compareTo (Object o)
  {
    ObjectStreamField f = (ObjectStreamField)o;
    boolean this_is_primitive = isPrimitive ();
    boolean f_is_primitive = f.isPrimitive ();

    if (this_is_primitive && !f_is_primitive)
      return -1;

    if (!this_is_primitive && f_is_primitive)
      return 1;

    return getName ().compareTo (f.getName ());
  }

  /**
   * This method is specific to classpath's implementation and so has the default
   * access. It changes the state of this field to "persistent". It means that
   * the field should not be changed when the stream is read (if it is not
   * explicitly specified using serialPersistentFields).
   *
   * @param persistent True if the field is persistent, false in the 
   * other cases.
   * @see #isPersistent()
   */
  void setPersistent(boolean persistent)
  {
    this.persistent = persistent;
  }

  /**
   * This method returns true if the field is marked as persistent.
   *
   * @return True if persistent, false in the other cases.
   * @see #setPersistent(boolean)
   */
  boolean isPersistent()
  {
    return persistent;
  }

  /**
   * This method is specific to classpath's implementation and so 
   * has the default access. It changes the state of this field as
   * to be set by ObjectInputStream.
   *
   * @param toset True if this field should be set, false in the other
   * cases.
   * @see #isToSet()
   */
  void setToSet(boolean toset)
  {
    this.toset = toset;
  }

  /**
   * This method returns true if the field is marked as to be
   * set.
   *
   * @return True if it is to be set, false in the other cases.
   * @see #setToSet(boolean)
   */
  boolean isToSet()
  {
    return toset;
  }

  /**
   * This method searches for its field reference in the specified class
   * object. It requests privileges. If an error occurs the internal field
   * reference is not modified.
   *
   * @throws NoSuchFieldException if the field name does not exist in this class.
   * @throws SecurityException if there was an error requesting the privileges.
   */
  void lookupField(Class clazz) throws NoSuchFieldException, SecurityException
  {
    final Field f = clazz.getDeclaredField(name);
    
    AccessController.doPrivileged(new PrivilegedAction()
      {
	public Object run()
	{
	  f.setAccessible(true);
	  return null;
	}
      });
    
    this.field = f;
  }

  public String toString ()
  {
    return "ObjectStreamField< " + type + " " + name + " >";
  }

  /*
   * These methods set the required field in the class instance obj.
   * They may throw NullPointerException if the field does not really exist.
   */

  final void setBooleanField(Object obj, boolean val) throws IOException
  {
    try
      {
	field.setBoolean(obj, val);
      }
    catch (IllegalArgumentException _)
      {
	throw new InvalidClassException("incompatible field type for " + 
					obj.getClass().getName() + "." + name);
      }
    catch(IllegalAccessException x)
      {
	throw new InternalError(x.getMessage());
      }
  }
  
  final void setByteField(Object obj, byte val) throws IOException
  {
    try
      {
	field.setByte(obj, val);
      }
    catch (IllegalArgumentException _)
      {
	throw new InvalidClassException("incompatible field type for " + 
					obj.getClass().getName() + "." + name);
      }
    catch(IllegalAccessException x)
      {
	throw new InternalError(x.getMessage());
      }
  }
  
  final void setCharField(Object obj, char val) throws IOException
  {
    try
      {
	field.setChar(obj, val);
      }
    catch (IllegalArgumentException _)
      {
	throw new InvalidClassException("incompatible field type for " + 
					obj.getClass().getName() + "." + name);
      }
    catch(IllegalAccessException x)
      {
	throw new InternalError(x.getMessage());
      }
  }
  
  final void setShortField(Object obj, short val) throws IOException
  {
    try
      {
	field.setShort(obj, val);
      }
    catch (IllegalArgumentException _)
      {
	throw new InvalidClassException("incompatible field type for " + 
					obj.getClass().getName() + "." + name);
      }
    catch(IllegalAccessException x)
      {
	throw new InternalError(x.getMessage());
      }
  }
  
  final void setIntField(Object obj, int val) throws IOException
  {
    try
      {
	field.setInt(obj, val);
      }
    catch (IllegalArgumentException _)
      {
	throw new InvalidClassException("incompatible field type for " + 
					obj.getClass().getName() + "." + name);
      }
    catch(IllegalAccessException x)
      {
	throw new InternalError(x.getMessage());
      }
    catch (Exception _)
      {
      }
  }
  
  final void setLongField(Object obj, long val) throws IOException
  {
    try
      {
	field.setLong(obj, val);
      }
    catch (IllegalArgumentException _)
      {
	throw new InvalidClassException("incompatible field type for " + 
					obj.getClass().getName() + "." + name);
      }
    catch(IllegalAccessException x)
      {
	throw new InternalError(x.getMessage());
      }
  }
  
  final void setFloatField(Object obj, float val) throws IOException
  {
    try
      {
	field.setFloat(obj, val);
      }
    catch (IllegalArgumentException _)
      {
	throw new InvalidClassException("incompatible field type for " + 
					obj.getClass().getName() + "." + name);
      }
    catch(IllegalAccessException x)
      {
	throw new InternalError(x.getMessage());
      }
  }
  
  final void setDoubleField(Object obj, double val) throws IOException
  {
    try
      {
	field.setDouble(obj, val);
      }
    catch (IllegalArgumentException _)
      {
	throw new InvalidClassException("incompatible field type for " + 
					obj.getClass().getName() + "." + name);
      }
    catch(IllegalAccessException x)
      {
	throw new InternalError(x.getMessage());
      }
  }
  
  final void setObjectField(Object obj, String valtype, Object val) throws IOException
  {
    if (valtype == null ||
	!typename.equals(valtype))
      throw new InvalidClassException("incompatible field type for " + 
				      obj.getClass().getName() + "." + name);
 
    try
      {
	field.set(obj, val);
      }
    catch(IllegalAccessException x)
      {
	throw new InternalError(x.getMessage());
      }
  }
}

/*******************************************************************************
 * Copyright (c) 2013, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *     Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *     Neither the name of Salesforce.com nor the names of its contributors may
 *     be used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package com.salesforce.phoenix.schema;

import java.sql.*;
import java.util.Arrays;
import java.util.Map;

/**
 * java.sql.Array implementation for Phoenix
 */
public class PhoenixArray implements Array {
	PDataType baseType;
	Object array;
	int dimensions;
	public PhoenixArray() {
		// empty constructor
	}
	
	public PhoenixArray(PDataType baseType, Object[] elements) {
		// As we are dealing with primitive types and only the Boxed objects
		this.baseType = baseType;
		this.array = convertObjectArrayToPrimitiveArray(elements);
		this.dimensions = elements.length;
	}
	
	public Object convertObjectArrayToPrimitiveArray(Object[] elements) {
		return elements;
	}

	@Override
	public void free() throws SQLException {
	}

	@Override
	public Object getArray() throws SQLException {
		return array;
	}

	@Override
	public Object getArray(Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException("Currently not supported");
	}

	@Override
	public Object getArray(long index, int count) throws SQLException {
		if(index < 1) {
			throw new IllegalArgumentException("Index cannot be less than 1");
		}
		// Get the set of elements from the given index to the specified count
		Object[] intArr = (Object[]) array;
		boundaryCheck(index, count, intArr);
		Object[] newArr = new Object[count];
		// Add checks() here.
		int i = 0;
		for (int j = (int) index; j < count; j++) {
			newArr[i] = intArr[j];
			i++;
		}
		return newArr;
	}

	private void boundaryCheck(long index, int count, Object[] arr) {
		if ((--index) + count > arr.length) {
			throw new IllegalArgumentException("The array index is out of range of the total number of elements in the array " + arr.length);
		}
	}

	@Override
	public Object getArray(long index, int count, Map<String, Class<?>> map)
			throws SQLException {
		if(map != null && !map.isEmpty()) {
			throw new UnsupportedOperationException("Currently not supported");
		}
		return null;
	}

	@Override
	public int getBaseType() throws SQLException {
		return baseType.getSqlType();
	}

	@Override
	public String getBaseTypeName() throws SQLException {
		return baseType.getSqlTypeName();
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		throw new UnsupportedOperationException("Currently not supported");
	}

	@Override
	public ResultSet getResultSet(Map<String, Class<?>> arg0)
			throws SQLException {
		throw new UnsupportedOperationException("Currently not supported");
	}

	@Override
	public ResultSet getResultSet(long arg0, int arg1) throws SQLException {
		throw new UnsupportedOperationException("Currently not supported");
	}

	@Override
	public ResultSet getResultSet(long arg0, int arg1,
			Map<String, Class<?>> arg2) throws SQLException {
		throw new UnsupportedOperationException("Currently not supported");
	}

	public int getDimensions() {
		return this.dimensions;
	}
	
	public int estimateByteSize(int pos) {
		return this.baseType.estimateByteSize(((Object[])array)[pos]);
	}
	
	public byte[] toBytes(int pos) {
		return this.baseType.toBytes(((Object[])array)[pos]);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this.dimensions != ((PhoenixArray) obj).dimensions) {
			return false;
		}
		if (this.baseType != ((PhoenixArray) obj).baseType) {
			return false;
		}
		return Arrays.deepEquals((Object[]) this.array,
				(Object[]) ((PhoenixArray) obj).array);
	}

	@Override
	public int hashCode() {
		// TODO : Revisit
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((array == null) ? 0 : array.hashCode());
		return result;
	}
	
	public static class PrimitiveIntPhoenixArray extends PhoenixArray {
		private int[] intArr;
		public PrimitiveIntPhoenixArray(PDataType dataType, Object[] elements) {
			super(dataType, elements);
		}
		@Override
		public Object convertObjectArrayToPrimitiveArray(Object[] elements) {
			Object object = java.lang.reflect.Array.newInstance(int.class,
					elements.length);
			intArr = (int[]) object;
			return intArr;
		}
		
		public int estimateByteSize(int pos) {
			return this.baseType.estimateByteSize(intArr[pos]);
		}
		
		public byte[] toBytes(int pos) {
			return this.baseType.toBytes((Integer)intArr[pos]);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this.dimensions != ((PhoenixArray) obj).dimensions) {
				return false;
			}
			if (this.baseType != ((PhoenixArray) obj).baseType) {
				return false;
			}
			return Arrays.equals((int[]) this.array,
					(int[]) ((PhoenixArray) obj).array);
		}
	}
	
	public static class PrimitiveShortPhoenixArray extends PhoenixArray {
		private short[] shortArr;
		public PrimitiveShortPhoenixArray(PDataType dataType, Object[] elements) {
			super(dataType, elements);
		}
		@Override
		public Object convertObjectArrayToPrimitiveArray(Object[] elements) {
			Object object = java.lang.reflect.Array.newInstance(short.class,
					elements.length);
			shortArr = (short[]) object;
			return shortArr;
		}
		
		public int estimateByteSize(int pos) {
			return this.baseType.estimateByteSize(shortArr[pos]);
		}
		
		public byte[] toBytes(int pos) {
			return this.baseType.toBytes((Short)shortArr[pos]);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this.dimensions != ((PhoenixArray) obj).dimensions) {
				return false;
			}
			if (this.baseType != ((PhoenixArray) obj).baseType) {
				return false;
			}
			return Arrays.equals((short[]) this.array,
					(short[]) ((PhoenixArray) obj).array);
		}
	}
	
	public static class PrimitiveLongPhoenixArray extends PhoenixArray {
		private long[] longArr;
		public PrimitiveLongPhoenixArray(PDataType dataType, Object[] elements) {
			super(dataType, elements);
		}
		@Override
		public Object convertObjectArrayToPrimitiveArray(Object[] elements) {
			Object object = java.lang.reflect.Array.newInstance(long.class,
					elements.length);
			longArr = (long[]) object;
			return longArr;
		}
		public int estimateByteSize(int pos) {
			return this.baseType.estimateByteSize(longArr[pos]);
		}
		
		public byte[] toBytes(int pos) {
			return this.baseType.toBytes((Long)longArr[pos]);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this.dimensions != ((PhoenixArray) obj).dimensions) {
				return false;
			}
			if (this.baseType != ((PhoenixArray) obj).baseType) {
				return false;
			}
			return Arrays.equals((long[]) this.array,
					(long[]) ((PhoenixArray) obj).array);
		}

	}
	
	public static class PrimitiveDoublePhoenixArray extends PhoenixArray {
		private double[] doubleArr;
		public PrimitiveDoublePhoenixArray(PDataType dataType, Object[] elements) {
			super(dataType, elements);
		}
		@Override
		public Object convertObjectArrayToPrimitiveArray(Object[] elements) {
			Object object = java.lang.reflect.Array.newInstance(double.class,
					elements.length);
			doubleArr = (double[]) object;
			return doubleArr;
		}
		
		public int estimateByteSize(int pos) {
			return this.baseType.estimateByteSize(doubleArr[pos]);
		}
		
		public byte[] toBytes(int pos) {
			return this.baseType.toBytes((Double)doubleArr[pos]);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this.dimensions != ((PhoenixArray) obj).dimensions) {
				return false;
			}
			if (this.baseType != ((PhoenixArray) obj).baseType) {
				return false;
			}
			return Arrays.equals((double[]) this.array,
					(double[]) ((PhoenixArray) obj).array);
		}
	}
	
	public static class PrimitiveFloatPhoenixArray extends PhoenixArray {
		private float[] floatArr;
		public PrimitiveFloatPhoenixArray(PDataType dataType, Object[] elements) {
			super(dataType, elements);
		}
		@Override
		public Object convertObjectArrayToPrimitiveArray(Object[] elements) {
			Object object = java.lang.reflect.Array.newInstance(float.class,
					elements.length);
			floatArr = (float[]) object;
			return floatArr;
		}
		
		public int estimateByteSize(int pos) {
			return this.baseType.estimateByteSize(floatArr[pos]);
		}
		
		public byte[] toBytes(int pos) {
			return this.baseType.toBytes((Float)floatArr[pos]);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this.dimensions != ((PhoenixArray) obj).dimensions) {
				return false;
			}
			if (this.baseType != ((PhoenixArray) obj).baseType) {
				return false;
			}
			return Arrays.equals((float[]) this.array,
					(float[]) ((PhoenixArray) obj).array);
		}
	}
	
	public static class PrimitiveBytePhoenixArray extends PhoenixArray {
		private byte[] byteArr;
		public PrimitiveBytePhoenixArray(PDataType dataType, Object[] elements) {
			super(dataType, elements);
		}
		@Override
		public Object convertObjectArrayToPrimitiveArray(Object[] elements) {
			Object object = java.lang.reflect.Array.newInstance(byte.class,
					elements.length);
			byteArr = (byte[]) object;
			return byteArr;
		}
		
		public int estimateByteSize(int pos) {
			return this.baseType.estimateByteSize(byteArr[pos]);
		}
		
		public byte[] toBytes(int pos) {
			return this.baseType.toBytes((Byte)byteArr[pos]);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this.dimensions != ((PhoenixArray) obj).dimensions) {
				return false;
			}
			if (this.baseType != ((PhoenixArray) obj).baseType) {
				return false;
			}
			return Arrays.equals((byte[]) this.array,
					(byte[]) ((PhoenixArray) obj).array);
		}
	}
	
	public static class PrimitiveBooleanPhoenixArray extends PhoenixArray {
		private boolean[] booleanArr;
		public PrimitiveBooleanPhoenixArray(PDataType dataType, Object[] elements) {
			super(dataType, elements);
		}
		@Override
		public Object convertObjectArrayToPrimitiveArray(Object[] elements) {
			Object object = java.lang.reflect.Array.newInstance(boolean.class,
					elements.length);
			booleanArr = (boolean[]) object;
			return booleanArr;
		}
		
		public int estimateByteSize(int pos) {
			return this.baseType.estimateByteSize(booleanArr[pos]);
		}
		
		public byte[] toBytes(int pos) {
			return this.baseType.toBytes((Boolean)booleanArr[pos]);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this.dimensions != ((PhoenixArray) obj).dimensions) {
				return false;
			}
			if (this.baseType != ((PhoenixArray) obj).baseType) {
				return false;
			}
			return Arrays.equals((boolean[]) this.array,
					(boolean[]) ((PhoenixArray) obj).array);
		}
	}
	
	public static class PrimitiveCharPhoenixArray extends PhoenixArray {
		private char[] charArr;
		public PrimitiveCharPhoenixArray(PDataType dataType, Object[] elements) {
			super(dataType, elements);
		}
		@Override
		public Object convertObjectArrayToPrimitiveArray(Object[] elements) {
			Object object = java.lang.reflect.Array.newInstance(char.class,
					elements.length);
			charArr = (char[]) object;
			return charArr;
		}
		
		public int estimateByteSize(int pos) {
			return this.baseType.estimateByteSize(charArr[pos]);
		}
		
		public byte[] toBytes(int pos) {
			return this.baseType.toBytes((Character)charArr[pos]);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this.dimensions != ((PhoenixArray) obj).dimensions) {
				return false;
			}
			if (this.baseType != ((PhoenixArray) obj).baseType) {
				return false;
			}
			return Arrays.equals((char[]) this.array,
					(char[]) ((PhoenixArray) obj).array);
		}
	}

	
}
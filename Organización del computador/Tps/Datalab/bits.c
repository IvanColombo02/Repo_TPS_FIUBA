/*
 * CS:APP Data Lab
 *
 * <Please put your name and userid here>
 *
 * bits.c - Source file with your solutions to the Lab.
 *          This is the file you will hand in to your instructor.
 *
 * WARNING: Do not include the <stdio.h> header; it confuses the dlc
 * compiler. You can still use printf for debugging without including
 * <stdio.h>, although you might get a compiler warning. In general,
 * it's not good practice to ignore compiler warnings, but in this
 * case it's OK.
 */

#if 0
/*
 * Instructions to Students:
 *
 * STEP 1: Read the following instructions carefully.
 */

You will provide your solution to the Data Lab by
editing the collection of functions in this source file.

INTEGER CODING RULES:

  Replace the "return" statement in each function with one
  or more lines of C code that implements the function. Your code
  must conform to the following style:

  int Funct(arg1, arg2, ...) {
      /* brief description of how your implementation works */
      int var1 = Expr1;
      ...
      int varM = ExprM;

      varJ = ExprJ;
      ...
      varN = ExprN;
      return ExprR;
  }

  Each "Expr" is an expression using ONLY the following:
  1. Integer constants 0 through 255 (0xFF), inclusive. You are
      not allowed to use big constants such as 0xffffffff.
  2. Function arguments and local variables (no global variables).
  3. Unary integer operations ! ~
  4. Binary integer operations & ^ | + << >>

  Some of the problems restrict the set of allowed operators even further.
  Each "Expr" may consist of multiple operators. You are not restricted to
  one operator per line.

  You are expressly forbidden to:
  1. Use any control constructs such as if, do, while, for, switch, etc.
  2. Define or use any macros.
  3. Define any additional functions in this file.
  4. Call any functions.
  5. Use any other operations, such as &&, ||, -, or ?:
  6. Use any form of casting.
  7. Use any data type other than int.  This implies that you
     cannot use arrays, structs, or unions.


  You may assume that your machine:
  1. Uses 2s complement, 32-bit representations of integers.
  2. Performs right shifts arithmetically.
  3. Has unpredictable behavior when shifting if the shift amount
     is less than 0 or greater than 31.


EXAMPLES OF ACCEPTABLE CODING STYLE:
  /*
   * pow2plus1 - returns 2^x + 1, where 0 <= x <= 31
   */
  int pow2plus1(int x) {
     /* exploit ability of shifts to compute powers of 2 */
     return (1 << x) + 1;
  }

  /*
   * pow2plus4 - returns 2^x + 4, where 0 <= x <= 31
   */
  int pow2plus4(int x) {
     /* exploit ability of shifts to compute powers of 2 */
     int result = (1 << x);
     result += 4;
     return result;
  }

FLOATING POINT CODING RULES

For the problems that require you to implement floating-point operations,
the coding rules are less strict.  You are allowed to use looping and
conditional control.  You are allowed to use both ints and unsigneds.
You can use arbitrary integer and unsigned constants. You can use any arithmetic,
logical, or comparison operations on int or unsigned data.

You are expressly forbidden to:
  1. Define or use any macros.
  2. Define any additional functions in this file.
  3. Call any functions.
  4. Use any form of casting.
  5. Use any data type other than int or unsigned.  This means that you
     cannot use arrays, structs, or unions.
  6. Use any floating point data types, operations, or constants.


NOTES:
  1. Use the dlc (data lab checker) compiler (described in the handout) to
     check the legality of your solutions.
  2. Each function has a maximum number of operations (integer, logical,
     or comparison) that you are allowed to use for your implementation
     of the function.  The max operator count is checked by dlc.
     Note that assignment ('=') is not counted; you may use as many of
     these as you want without penalty.
  3. Use the btest test harness to check your functions for correctness.
  4. Use the BDD checker to formally verify your functions
  5. The maximum number of ops for each function is given in the
     header comment for each function. If there are any inconsistencies
     between the maximum ops in the writeup and in this file, consider
     this file the authoritative source.

/*
 * STEP 2: Modify the following functions according the coding rules.
 *
 *   IMPORTANT. TO AVOID GRADING SURPRISES:
 *   1. Use the dlc compiler to check that your solutions conform
 *      to the coding rules.
 *   2. Use the BDD checker to formally verify that your solutions produce
 *      the correct answers.
 */


#endif
/* 
 * bitOr - x|y using only ~ and & 
 *   Example: bitOr(6, 5) = 7
 *   Legal ops: ~ &
 *   Max ops: 8
 *   Rating: 1
 */
int bitOr(int x, int y) {
  return ~(~x & ~y);
}
/*
 * bitParity - returns 1 if x contains an odd number of 0's
 *   Examples: bitParity(5) = 0, bitParity(7) = 1
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 20
 *   Rating: 4
 */
int bitParity(int x) {
  x = x ^ (x >> 16); 
  x = x ^ (x >> 8);   
  x = x ^ (x >> 4);  
  x = x ^ (x >> 2);   
  x = x ^ (x >> 1);   
  return x & 1;
}
/* 
 * bitNor - ~(x|y) using only ~ and & 
 *   Example: bitNor(0x6, 0x5) = 0xFFFFFFF8
 *   Legal ops: ~ &
 *   Max ops: 8
 *   Rating: 1
 */
int bitNor(int x, int y) {
  return ~x & ~y;
}
/* 
 * bitXor - x^y using only ~ and & 
 *   Example: bitXor(4, 5) = 1
 *   Legal ops: ~ &
 *   Max ops: 14
 *   Rating: 1
 */
int bitXor(int x, int y) {
  return ~(~(~x & y) & ~(x & ~y));
}
//2
/* 
 * evenBits - return word with all even-numbered bits set to 1
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 8
 *   Rating: 1
 */
int evenBits(void) {
  int mask = 0x55; // 01010101 
  mask = (mask << 8) | mask; 
  mask = (mask << 16) | mask; 
  return mask;
}
/* 
 * anyOddBit - return 1 if any odd-numbered bit in word set to 1
 *   where bits are numbered from 0 (least significant) to 31 (most significant)
 *   Examples anyOddBit(0x5) = 0, anyOddBit(0x7) = 1
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 12
 *   Rating: 2
 */

int anyOddBit(int x) {
int mask = 0xAA;
  mask = (mask << 8) | mask;
  mask = (mask << 16) | mask;
  return !!(mask & x);
}
/* 
 * byteSwap - swaps the nth byte and the mth byte
 *  Examples: byteSwap(0x12345678, 1, 3) = 0x56341278
 *            byteSwap(0xDEADBEEF, 0, 2) = 0xDEEFBEAD
 *  You may assume that 0 <= n <= 3, 0 <= m <= 3
 *  Legal ops: ! ~ & ^ | + << >>
 *  Max ops: 25
 *  Rating: 2
 */
int byteSwap(int x, int n, int m) {
  // Esta funcion guarda los bytes recibidos como n y m y luego crea una mascara 
  // en las posiciones donde estaban n y m para luego ser swapeadas
  int pos_n = n << 3;
  int pos_m = m << 3;
  int byte_n = (x >> pos_n) & 0xFF; 
  int byte_m = (x >> pos_m) & 0xFF; 
  int mask = ~((0xFF << pos_n) | (0xFF << pos_m)); 
  // pasamos la mascara en cada posicion de x y le pasamos el byte_n en la pos_m y viceversa
  x = (x & mask) | (byte_n << pos_m) | (byte_m << pos_n); 
  return x;
}
/* 
 * fitsBits - return 1 if x can be represented as an 
 *  n-bit, two's complement integer.
 *   1 <= n <= 32
 *   Examples: fitsBits(5,3) = 0, fitsBits(-4,3) = 1
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 15
 *   Rating: 2
 */
int fitsBits(int x, int n) {
  int shift = 32 + ~n + 1;
  int shift_derecha = x << shift;
  int shift_total = shift_derecha >> shift;
  return !(x ^ shift_total);
}
/* 
 * oddBits - return word with all odd-numbered bits set to 1
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 8
 *   Rating: 2
 */
int oddBits(void) {
  int mask = 0xAA;
  mask = (mask << 8) | mask;
  mask = (mask << 16) | mask;
  return mask;
}
/* 
 * sign - return 1 if positive, 0 if zero, and -1 if negative
 *  Examples: sign(130) = 1
 *            sign(-23) = -1
 *  Legal ops: ! ~ & ^ | + << >>
 *  Max ops: 10
 *  Rating: 2
 */
int sign(int x) {
    int sign_1 = x >> 31; //devuelve 0 si es positivo o -1 si es negativo
    return ((!x) ^ !(sign_1)) + sign_1;
}
//3
/* 
 * addOK - Determine if can compute x+y without overflow
 *   Example: addOK(0x80000000,0x80000000) = 0,
 *            addOK(0x80000000,0x70000000) = 1, 
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 20
 *   Rating: 3
 */
int addOK(int x, int y) {
  int x_signo = x >> 31;
  int y_signo = y >> 31;
  int sum_signo = (x + y) >> 31;
  
  int mismo_signo = !(x_signo ^ y_signo);
  int overflow = x_signo ^ sum_signo ;

  return !(mismo_signo & overflow);
}
/* 
 * bitMask - Generate a mask consisting of all 1's 
 *   lowbit and highbit
 *   Examples: bitMask(5,3) = 0x38
 *   Assume 0 <= lowbit <= 31, and 0 <= highbit <= 31
 *   If lowbit > highbit, then mask should be all 0's
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 16
 *   Rating: 3
 */
int bitMask(int highbit, int lowbit) {
  int mask = ~0; 
  int low_mask = mask << lowbit;  
  int high_mask = (mask << (highbit + 1)) ;  
  //Reviso si por alguna razon high_mask es todos 0, para cancelarlo o no
  int result =  (high_mask + !(high_mask ^ mask)) | ~low_mask;  
  return ~result; 
}

/* 
 * conditional - same as x ? y : z 
 *   Example: conditional(2,4,5) = 4
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 16
 *   Rating: 3
 */
int conditional(int x, int y, int z) {
  int mask = !!x + ~0;
  return (y & ~mask) + (z & mask);
}
/*
 * bitCount - returns count of number of 1's in word
 *   Examples: bitCount(5) = 2, bitCount(7) = 3
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 40
 *   Rating: 4
 */
int bitCount(int x) {
  //de manera escalonada la funcion suma los bits de x con un algoritmo de bit population count
  int mask1 = 0x55 | (0x55 << 8); 
  int mask2 = 0x33 | (0x33 << 8); 
  int mask3 = 0x0F | (0x0F << 8); 
  int mask4 = 0xFF | (0xFF << 16); 
  int mask5 = 0xFF | (0xFF << 8); 
  mask1 = mask1 | (mask1 << 16);
  mask2 = mask2 | (mask2 << 16);
  mask3 = mask3 | (mask3 << 16);

  x = (x & mask1) + ((x >> 1) & mask1);
  x = (x & mask2) + ((x >> 2) & mask2);
  x = (x & mask3) + ((x >> 4) & mask3);
  x = (x & mask4) + ((x >> 8) & mask4);
  x = (x & mask5) + ((x >> 16) & mask5);

  return x;
}
/* 
 * bitMatch - Create mask indicating which bits in x match those in y
 *            using only ~ and & 
 *   Example: bitMatch(0x7, 0xE) = 0x6
 *   Legal ops: ~ & |
 *   Max ops: 14
 *   Rating: 1
 */
int bitMatch(int x, int y) {
  int mask_ones = x & y;
  int mask_zeros = ~x & ~y;
  return mask_ones | mask_zeros;
}

/* 
 * replaceByte(x,n,c) - Replace byte n in x with c
 *   Bytes numbered from 0 (LSB) to 3 (MSB)
 *   Examples: replaceByte(0x12345678,1,0xab) = 0x1234ab78
 *   You can assume 0 <= n <= 3 and 0 <= c <= 255
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 10
 *   Rating: 3
 */
int replaceByte(int x, int n, int c) {
  int pos_n = n << 3;
  int mask = ~(0xFF << pos_n);
  x = (x & mask) | (c << pos_n);
  return x;
}
//4
/*
 * satAdd - adds two numbers but when positive overflow occurs, returns
 *          maximum possible value, and when negative overflow occurs,
 *          it returns minimum negative value.
 *   Examples: satAdd(0x40000000,0x40000000) = 0x7fffffff
 *             satAdd(0x80000000,0xffffffff) = 0x80000000
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 30
 *   Rating: 4
 */
int satAdd(int x, int y) {
  int sum = x + y;
  int pos_overflow = ~(x >> 31) & ~(y >> 31) & (sum >> 31);
  int neg_overflow = (x >> 31) & (y >> 31) & ~(sum >> 31);
  int pos = ~(1 << 31);
  int neg = 1 << 31;

  return (pos_overflow & pos) | (neg_overflow & neg) | (~(pos_overflow | neg_overflow) & sum);
}
/*
 * satMul2 - multiplies by 2, saturating to Tmin or Tmax if overflow
 *   Examples: satMul2(0x30000000) = 0x60000000
 *             satMul2(0x40000000) = 0x7FFFFFFF (saturate to TMax)
 *             satMul2(0x80000001) = 0x80000000 (saturate to TMin)
 *   Legal ops: ! ~ & ^ | + << >>
 *   Max ops: 20
 *   Rating: 3
 */
int satMul2(int x) {
  int result = x << 1;                   
  int sign = x >> 31;                    
  int sign_result = result >> 31;          
  int overflow = sign ^ sign_result;   //Esto devuelve -1 (hay) o 0 (no hay)
  int Tmin = 0x8 << 28;                       
  // Si hay overflow, devolver Tmax o Tmin según el signo de x
  return (overflow & (sign ^ (~Tmin))) | (result & ~overflow);
  // ~overflow & result lo que hace es que si existe overflow elimina result
  // (sign ^ Tmax) si sing es -1 devuelve 1000... sino devuelve el 0111...
}
/* 
 * isNonZero - Check whether x is nonzero using
 *              the legal operators except !
 *   Examples: isNonZero(3) = 1, isNonZero(0) = 0
 *   Legal ops: ~ & ^ | + << >>
 *   Max ops: 10
 *   Rating: 4 
 */
int isNonZero(int x) {
  int signo = x >> 31;
  int x_compl = ~x + 1;
  int shift_lsb = (x_compl >> 31);
  return ~(signo | shift_lsb)+1;
}
/* 
 * rotateRight - Rotate x to the right by n
 *   Can assume that 0 <= n <= 31
 *   Examples: rotateRight(0x87654321,4) = 0x187654321
 *   Legal ops: ~ & ^ | + << >> !
 *   Max ops: 25
 *   Rating: 3 
 */
int rotateRight(int x, int n) {
  //el ejemplo del comentario es incorrecto pero no voy a modificarlo, deberia ser 0x18765432.
  int shift = 32 + (~n + 1);
  int shift_derecha = (x >> n) & ((1 << shift) + ~0);
  int shift_izquierda = x << shift;
  
  return shift_derecha | shift_izquierda;
}
//float
/* 
 * floatAbsVal - Return bit-level equivalent of absolute value of f for
 *   floating point argument f.
 *   Both the argument and result are passed as unsigned int's, but
 *   they are to be interpreted as the bit-level representations of
 *   single-precision floating point values.
 *   When argument is NaN, return argument..
 *   Legal ops: Any integer/unsigned operations incl. ||, &&. also if, while
 *   Max ops: 10
 *   Rating: 2
 */
unsigned floatAbsVal(unsigned uf) {
  unsigned mask = 0x7FFFFFFF; 
  unsigned abs_val = uf & mask; 
  //Chekeo de NaN
  unsigned mantissa = uf & 0x7FFFFF;
  unsigned exponent = (uf >> 23) & 0xFF;
  if (exponent == 0xFF && mantissa != 0)
    return uf;
  return abs_val; 
}
/* 
 * floatIsEqual - Compute f == g for floating point arguments f and g.
 *   Both the arguments are passed as unsigned int's, but
 *   they are to be interpreted as the bit-level representations of
 *   single-precision floating point values.
 *   If either argument is NaN, return 0.
 *   +0 and -0 are considered equal.
 *   Legal ops: Any integer/unsigned operations incl. ||, &&. also if, while
 *   Max ops: 25
 *   Rating: 2
 */
int floatIsEqual(unsigned uf, unsigned ug) {
  int uf_exp = (uf & 0x7F800000) >>23;
  int uf_mantisa = uf & 0x007FFFFF;
  int ug_exp = (ug & 0x7F800000) >>23;
  int ug_mantisa = ug & 0x007FFFFF;
  if ((uf_exp == 0 && ug_exp == 0) && (uf_mantisa == 0 && ug_mantisa == 0)){
    return 1;
  }
  if (uf_exp == 0xFF && uf_mantisa != 0){
    return 0;
  }
  if (ug_exp == 0xFF && ug_mantisa != 0){
    return 0;
  }
   if (uf == ug){
    return 1;
  }
  return 0;
}
/* 
 * floatNegate - Return bit-level equivalent of expression -f for
 *   floating point argument f.
 *   Both the argument and result are passed as unsigned int's, but
 *   they are to be interpreted as the bit-level representations of
 *   single-precision floating point values.
 *   When argument is NaN, return argument.
 *   Legal ops: Any integer/unsigned operations incl. ||, &&. also if, while
 *   Max ops: 10
 *   Rating: 2
 */
unsigned floatNegate(unsigned uf) {
  // mask para verificar NaN, todo 1
  unsigned nan = 0x7F800000; 

  if ((uf & nan) == nan && (uf & 0x007FFFFF) != 0) {
    return uf; 
  }

  // invierte el bit del signo
  return uf ^ 0x80000000; 
}
/* 
 * floatIsLess - Compute f < g for floating point arguments f and g.
 *   Both the arguments are passed as unsigned int's, but
 *   they are to be interpreted as the bit-level representations of
 *   single-precision floating point values.
 *   If either argument is NaN, return 0.
 *   +0 and -0 are considered equal.
 *   Legal ops: Any integer/unsigned operations incl. ||, &&. also if, while
 *   Max ops: 30
 *   Rating: 3
 */
int floatIsLess(unsigned uf, unsigned ug) {
  unsigned mantissa_uf = uf & 0x7FFFFF;
  unsigned exponent_uf = (uf >> 23) & 0xFF;
  unsigned mantissa_ug = ug & 0x7FFFFF;
  unsigned exponent_ug = (ug >> 23) & 0xFF;
  unsigned sign_uf = uf >> 31;
  unsigned sign_ug = ug >> 31;
  if (mantissa_uf == 0 && exponent_uf == 0 && mantissa_ug == 0 && exponent_ug == 0)return 0;
  if (exponent_uf == 0xFF && mantissa_uf != 0) return 0; 
  if (exponent_ug == 0xFF && mantissa_ug != 0) return 0; 

  if (sign_uf != sign_ug) return sign_uf > sign_ug; // uf positivo, ug negativo
  // Comparar exponentes
  if (exponent_uf != exponent_ug) return sign_uf ? exponent_uf > exponent_ug : exponent_uf < exponent_ug;
  // Comparar mantisas
  if (mantissa_uf != mantissa_ug) return sign_uf ? mantissa_uf > mantissa_ug : mantissa_uf < mantissa_ug;
  return 0; 
}
/* 
 * floatFloat2Int - Return bit-level equivalent of expression (int) f
 *   for floating point argument f.
 *   Argument is passed as unsigned int, but
 *   it is to be interpreted as the bit-level representation of a
 *   single-precision floating point value.
 *   Anything out of range (including NaN and infinity) should return
 *   0x80000000u.
 *   Legal ops: Any integer/unsigned operations incl. ||, &&. also if, while
 *   Max ops: 30
 *   Rating: 4
 */
int floatFloat2Int(unsigned uf) {
  int res = 0;
  int uf_exp = (uf & 0x7F800000) >>23;
  int uf_signo = uf >> 31;
  int uf_mantisa = uf & 0x007FFFFF;
  int exp_normalizado = uf_exp - 127; //
  int mantisa_normalizada = uf_mantisa | 0x00800000; // redondeo la mantisa
  if (uf_exp == 0xFF  || exp_normalizado > 31){ // chequeo casos de overflow
    return 0x80000000u;
  }
  if (uf_exp == 0 || exp_normalizado < 0){ // casos de exponente menor o igual que 0
    return 0;
  }
  if (exp_normalizado >= 23) {
    res = mantisa_normalizada << (exp_normalizado-23);
  } else {
    res = mantisa_normalizada >>(23-exp_normalizado);
  }
  if (uf_signo == 1) {
    return -res;
  }
  return res;
}
/* 
 * floatPower2 - Return bit-level equivalent of the expression 2.0^x
 *   (2.0 raised to the power x) for any 32-bit integer x.
 *
 *   The unsigned value that is returned should have the identical bit
 *   representation as the single-precision floating-point number 2.0^x.
 *   If the result is too small to be represented as a denorm, return
 *   0. If too large, return +INF.
 * 
 *   Legal ops: Any integer/unsigned operations incl. ||, &&. Also if, while 
 *   Max ops: 30 
 *   Rating: 4
 */
unsigned floatPower2(int x) {
    if (x < -126) {
        return 0;  
    } else if (x > 127) {
        return 0x7F800000; 
    } else {
        return (x + 127) << 23; 
    }
}

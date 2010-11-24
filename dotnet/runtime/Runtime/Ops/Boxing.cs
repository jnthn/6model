using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;
using Rakudo.Metamodel.Representations;

namespace Rakudo.Runtime
{
    /// <summary>
    /// This class implements the various vm::op options that are
    /// available.
    /// </summary>
    public static partial class Ops
    {
        /// <summary>
        /// Boxes a native int into its matching value type.
        /// </summary>
        /// <param name="Value"></param>
        /// <returns></returns>
        public static RakudoObject box_int(ThreadContext TC, int Value, RakudoObject To)
        {
            var REPR = To.STable.REPR;
            var Result = REPR.instance_of(TC, To);
            REPR.set_int(TC, Result, Value);
            return Result;
        }

        /// <summary>
        /// Boxes a native int into its matching value type.
        /// </summary>
        /// <param name="Value"></param>
        /// <returns></returns>
        public static RakudoObject box_int(ThreadContext TC, int Value)
        {
            var Result = TC.DefaultIntBoxType.STable.REPR.instance_of(TC, TC.DefaultIntBoxType);
            TC.DefaultIntBoxType.STable.REPR.set_int(TC, Result, Value);
            return Result;
        }

        /// <summary>
        /// Boxes a native num into its matching value type.
        /// </summary>
        /// <param name="Value"></param>
        /// <returns></returns>
        public static RakudoObject box_num(ThreadContext TC, double Value, RakudoObject To)
        {
            var REPR = To.STable.REPR;
            var Result = REPR.instance_of(TC, To);
            REPR.set_num(TC, Result, Value);
            return Result;
        }

        /// <summary>
        /// Boxes a native num into its matching value type.
        /// </summary>
        /// <param name="Value"></param>
        /// <returns></returns>
        public static RakudoObject box_num(ThreadContext TC, int Value)
        {
            var Result = TC.DefaultNumBoxType.STable.REPR.instance_of(TC, TC.DefaultNumBoxType);
            TC.DefaultNumBoxType.STable.REPR.set_num(TC, Result, Value);
            return Result;
        }

        /// <summary>
        /// Boxes a native string into its matching value type.
        /// </summary>
        /// <param name="Value"></param>
        /// <returns></returns>
        public static RakudoObject box_str(ThreadContext TC, string Value, RakudoObject To)
        {
            var REPR = To.STable.REPR;
            var Result = REPR.instance_of(TC, To);
            REPR.set_str(TC, Result, Value);
            return Result;
        }

        /// <summary>
        /// Boxes a native string into its matching value type.
        /// </summary>
        /// <param name="Value"></param>
        /// <returns></returns>
        public static RakudoObject box_str(ThreadContext TC, string Value)
        {
            var Result = TC.DefaultStrBoxType.STable.REPR.instance_of(TC, TC.DefaultStrBoxType);
            TC.DefaultStrBoxType.STable.REPR.set_str(TC, Result, Value);
            return Result;
        }

        /// <summary>
        /// Unboxes a boxed int.
        /// </summary>
        /// <param name="Boxed"></param>
        /// <returns></returns>
        public static int unbox_int(ThreadContext TC, RakudoObject Boxed)
        {
            return Boxed.STable.REPR.get_int(TC, Boxed);
        }

        /// <summary>
        /// Unboxes a boxed num.
        /// </summary>
        /// <param name="Boxed"></param>
        /// <returns></returns>
        public static double unbox_num(ThreadContext TC, RakudoObject Boxed)
        {
            return Boxed.STable.REPR.get_num(TC, Boxed);
        }

        /// <summary>
        /// Unboxes a boxed string.
        /// </summary>
        /// <param name="Boxed"></param>
        /// <returns></returns>
        public static string unbox_str(ThreadContext TC, RakudoObject Boxed)
        {
            return Boxed.STable.REPR.get_str(TC, Boxed);
        }
    }
}

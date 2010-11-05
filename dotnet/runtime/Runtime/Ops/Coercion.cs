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
        /// Coerces an integer into a string.
        /// </summary>
        /// <param name="Int"></param>
        /// <param name="TargetType"></param>
        /// <returns></returns>
        public static RakudoObject coerce_int_to_str(ThreadContext TC, RakudoObject Int, RakudoObject TargetType)
        {
            int Value = Ops.unbox_int(TC, Int);
            return Ops.box_str(TC, Value.ToString(), TargetType);
        }

        /// <summary>
        /// Coerces a floating point number into a string.
        /// </summary>
        /// <param name="Num"></param>
        /// <param name="TargetType"></param>
        /// <returns></returns>
        public static RakudoObject coerce_num_to_str(ThreadContext TC, RakudoObject Num, RakudoObject TargetType)
        {
            double Value = Ops.unbox_num(TC, Num);
            return Ops.box_str(TC, Value.ToString(), TargetType);
        }

        /// <summary>
        /// Coerces an integer into a floating point number.
        /// </summary>
        /// <param name="Int"></param>
        /// <param name="TargetType"></param>
        /// <returns></returns>
        public static RakudoObject coerce_int_to_num(ThreadContext TC, RakudoObject Int, RakudoObject TargetType)
        {
            int Value = Ops.unbox_int(TC, Int);
            return Ops.box_num(TC, (double)Value, TargetType);
        }

        /// <summary>
        /// Coerces a floating point number into an integer.
        /// </summary>
        /// <param name="Int"></param>
        /// <param name="TargetType"></param>
        /// <returns></returns>
        public static RakudoObject coerce_num_to_int(ThreadContext TC, RakudoObject Num, RakudoObject TargetType)
        {
            double Value = Ops.unbox_num(TC, Num);
            return Ops.box_int(TC, (int)Value, TargetType);
        }

        /// <summary>
        /// Coerces a string into an integer.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="Str"></param>
        /// <param name="TargetType"></param>
        /// <returns></returns>
        public static RakudoObject coerce_str_to_int(ThreadContext TC, RakudoObject Str, RakudoObject TargetType)
        {
            int Value = 0;
            int.TryParse(Ops.unbox_str(TC, Str), out Value);
            return Ops.box_int(TC, Value, TargetType);
        }

        /// <summary>
        /// Coerces a string into an number.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="Str"></param>
        /// <param name="TargetType"></param>
        /// <returns></returns>
        public static RakudoObject coerce_str_to_num(ThreadContext TC, RakudoObject Str, RakudoObject TargetType)
        {
            double Value = 0;
            double.TryParse(Ops.unbox_str(TC, Str), out Value);
            return Ops.box_num(TC, Value, TargetType);
        }
    }
}

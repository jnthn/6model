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
        /// Logical not.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject logical_not_int(ThreadContext TC, RakudoObject x)
        {
            return Ops.box_int(TC, Ops.unbox_int(TC, x) == 0 ? 1 : 0, TC.DefaultBoolBoxType);
        }

        /// <summary>
        /// Performs an integer addition.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject add_int(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC, Ops.unbox_int(TC, x) + Ops.unbox_int(TC, y), TC.DefaultIntBoxType);
        }

        /// <summary>
        /// Performs an integer subtraction.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject sub_int(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC, Ops.unbox_int(TC, x) - Ops.unbox_int(TC, y), TC.DefaultIntBoxType);
        }

        /// <summary>
        /// Performs an integer multiplication.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject mul_int(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC, Ops.unbox_int(TC, x) * Ops.unbox_int(TC, y), TC.DefaultIntBoxType);
        }

        /// <summary>
        /// Performs an integer division.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject div_int(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC, Ops.unbox_int(TC, x) / Ops.unbox_int(TC, y), TC.DefaultIntBoxType);
        }

        /// <summary>
        /// Performs an integer modulo.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject mod_int(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC, Ops.unbox_int(TC, x) % Ops.unbox_int(TC, y), TC.DefaultIntBoxType);
        }

        /// <summary>
        /// Performs a string concatenation.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject concat(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_str(TC, Ops.unbox_str(TC, x) + Ops.unbox_str(TC, y), TC.DefaultStrBoxType);
        }
    }
}

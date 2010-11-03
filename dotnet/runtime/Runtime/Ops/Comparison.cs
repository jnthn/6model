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
        /// Compares two floating point numbers for equality.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject equal_nums(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC,
                (Ops.unbox_num(TC, x) == Ops.unbox_num(TC, y) ? 1 : 0),
                TC.DefaultBoolBoxType);
        }

        /// <summary>
        /// Compares two integers for equality.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject equal_ints(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC,
                (Ops.unbox_int(TC, x) == Ops.unbox_int(TC, y) ? 1 : 0),
                TC.DefaultBoolBoxType);
        }

        /// <summary>
        /// Compares two strings for equality.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject equal_strs(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC,
                (Ops.unbox_str(TC, x) == Ops.unbox_str(TC, y) ? 1 : 0),
                TC.DefaultBoolBoxType);
        }

        /// <summary>
        /// Compares reference equality.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <returns></returns>
        public static RakudoObject equal_refs(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC, x == y ? 1 : 0, TC.DefaultBoolBoxType);
        }

        /// <summary>
        /// Compares two floating point numbers for less-than inequality.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject less_than_nums(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC,
                (Ops.unbox_num(TC, x) < Ops.unbox_num(TC, y) ? 1 : 0),
                TC.DefaultBoolBoxType);
        }

        /// <summary>
        /// Compares two integers for less-than inequality.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject less_than_ints(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC,
                (Ops.unbox_int(TC, x) < Ops.unbox_int(TC, y) ? 1 : 0),
                TC.DefaultBoolBoxType);
        }

        /// <summary>
        /// Compares two floating point numbers for less-than-or-equal inequality.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject less_than_or_equal_nums(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC,
                (Ops.unbox_num(TC, x) <= Ops.unbox_num(TC, y) ? 1 : 0),
                TC.DefaultBoolBoxType);
        }

        /// <summary>
        /// Compares two integers for less-than-or-equal inequality.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject less_than_or_equal_ints(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC,
                (Ops.unbox_int(TC, x) <= Ops.unbox_int(TC, y) ? 1 : 0),
                TC.DefaultBoolBoxType);
        }

        /// <summary>
        /// Compares two floating point numbers for greater-than inequality.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject greater_than_nums(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC,
                (Ops.unbox_num(TC, x) > Ops.unbox_num(TC, y) ? 1 : 0),
                TC.DefaultBoolBoxType);
        }

        /// <summary>
        /// Compares two integers for greater-than inequality.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject greater_than_ints(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC,
                (Ops.unbox_int(TC, x) > Ops.unbox_int(TC, y) ? 1 : 0),
                TC.DefaultBoolBoxType);
        }

        /// <summary>
        /// Compares two floating point numbers for greater-than-or-equal inequality.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject greater_than_or_equal_nums(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC,
                (Ops.unbox_num(TC, x) >= Ops.unbox_num(TC, y) ? 1 : 0),
                TC.DefaultBoolBoxType);
        }

        /// <summary>
        /// Compares two integers for greater-than-or-equal inequality.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject greater_than_or_equal_ints(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC,
                (Ops.unbox_int(TC, x) >= Ops.unbox_int(TC, y) ? 1 : 0),
                TC.DefaultBoolBoxType);
        }
    }
}

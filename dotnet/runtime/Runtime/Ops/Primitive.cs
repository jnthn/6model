using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
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
        /// Performs a numeric addition.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject add_num(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_num(TC, Ops.unbox_num(TC, x) + Ops.unbox_num(TC, y), TC.DefaultNumBoxType);
        }

        /// <summary>
        /// Performs a numeric subtraction.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject sub_num(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_num(TC, Ops.unbox_num(TC, x) - Ops.unbox_num(TC, y), TC.DefaultNumBoxType);
        }

        /// <summary>
        /// Performs a numeric multiplication.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject mul_num(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_num(TC, Ops.unbox_num(TC, x) * Ops.unbox_num(TC, y), TC.DefaultNumBoxType);
        }

        /// <summary>
        /// Performs a numeric division.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject div_num(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_num(TC, Ops.unbox_num(TC, x) / Ops.unbox_num(TC, y), TC.DefaultNumBoxType);
        }

        /// <summary>
        /// Performs a bitwise or on ints.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject bitwise_or_int(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC, Ops.unbox_int(TC, x) | Ops.unbox_int(TC, y), TC.DefaultIntBoxType);
        }

        /// <summary>
        /// Performs a bitwise and on ints.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject bitwise_and_int(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC, Ops.unbox_int(TC, x) & Ops.unbox_int(TC, y), TC.DefaultIntBoxType);
        }

        /// <summary>
        /// Performs a bitwise xor on ints.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject bitwise_xor_int(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC, Ops.unbox_int(TC, x) ^ Ops.unbox_int(TC, y), TC.DefaultIntBoxType);
        }

        /// <summary>
        /// Performs a bitwise or on nums.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject bitwise_or_num(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_num(TC, BitConverter.Int64BitsToDouble(BitConverter.DoubleToInt64Bits(Ops.unbox_num(TC, x)) | BitConverter.DoubleToInt64Bits(Ops.unbox_num(TC, y))), TC.DefaultNumBoxType);
        }

        /// <summary>
        /// Performs a bitwise and on nums.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject bitwise_and_num(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_num(TC, BitConverter.Int64BitsToDouble(BitConverter.DoubleToInt64Bits(Ops.unbox_num(TC, x)) & BitConverter.DoubleToInt64Bits(Ops.unbox_num(TC, y))), TC.DefaultNumBoxType);
        }

        /// <summary>
        /// Performs a bitwise xor on nums.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject bitwise_xor_num(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_num(TC, BitConverter.Int64BitsToDouble(BitConverter.DoubleToInt64Bits(Ops.unbox_num(TC, x)) ^ BitConverter.DoubleToInt64Bits(Ops.unbox_num(TC, y))), TC.DefaultNumBoxType);
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

        /// <summary>
        /// Performs a string substring.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="z"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject substr(ThreadContext TC, RakudoObject x, RakudoObject y, RakudoObject z)
        {
            return Ops.box_str(TC, z.STable.REPR.defined(TC, z)
                ? Ops.unbox_str(TC, x).Substring(Ops.unbox_int(TC, y), Ops.unbox_int(TC, z))
                : Ops.unbox_str(TC, x).Substring(Ops.unbox_int(TC, y)), TC.DefaultStrBoxType);
        }

        /// <summary>
        /// Performs a string substring.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject substr(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_str(TC, Ops.unbox_str(TC, x).Substring(Ops.unbox_int(TC, y)), TC.DefaultStrBoxType);
        }

        /// <summary>
        /// Search for the first occurrence of a substring within a string and returns its zero-based index
        /// if it's found and -1 if it's not.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject index_str(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            return Ops.box_int(TC, Ops.unbox_str(TC, x).IndexOf(Ops.unbox_str(TC, y)), TC.DefaultIntBoxType);
        }

        /// <summary>
        /// Search for the first occurrence of a substring within a string and returns its zero-based index
        /// if it's found and -1 if it's not, starting at a specified index.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="z"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject index_str_index(ThreadContext TC, RakudoObject x, RakudoObject y, RakudoObject z)
        {
            return Ops.box_int(TC, Ops.unbox_str(TC, x).IndexOf(Ops.unbox_str(TC, y), Ops.unbox_int(TC, z)), TC.DefaultIntBoxType);
        }

        /// <summary>
        /// Split the first string by the first character of the second string returning an NQPList of the results.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject split_str(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            RakudoObject list = Ops.instance_of(TC, Ops.get_lex(TC, "NQPList"));
            var store = ((P6list.Instance)list).Storage;
            foreach (string splitted in Ops.unbox_str(TC, x).Split(Ops.unbox_str(TC, y)[0]))
                store.Add(Ops.box_str(TC, splitted, TC.DefaultStrBoxType));
            return list;
        }

        /// <summary>
        /// Returns the length of the string.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject length_str(ThreadContext TC, RakudoObject x)
        {
            return Ops.box_int(TC, Ops.unbox_str(TC, x).Length, TC.DefaultIntBoxType);
        }

        /// <summary>
        /// Checks whether a character at a particular index in a string
        /// is a member of a particular character class.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="z"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject is_cclass_str_index(ThreadContext TC, RakudoObject x, RakudoObject y, RakudoObject z)
        {
            CCLASS cclass = (CCLASS)Enum.Parse(typeof(CCLASS), Ops.unbox_str(TC, x));
            string target = Ops.unbox_str(TC, y);
            int index = Ops.unbox_int(TC, z);
            return Ops.box_int(TC, is_cclass(target, index, cclass) ? 1 : 0, TC.DefaultIntBoxType);
        }

        /// <summary>
        /// Checks whether a character is a member of a particular character class.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject is_cclass_str(ThreadContext TC, RakudoObject x, RakudoObject y)
        {
            CCLASS cclass = (CCLASS)Enum.Parse(typeof(CCLASS), Ops.unbox_str(TC, x));
            string target = Ops.unbox_str(TC, y);
            return Ops.box_int(TC, is_cclass(target, 0, cclass) ? 1 : 0, TC.DefaultIntBoxType);
        }

        // see http://msdn.microsoft.com/en-us/library/20bw873z.aspx
        // more precisely http://msdn.microsoft.com/en-us/library/20bw873z.aspx#SupportedUnicodeGeneralCategories
        // to add more categories/patterns.
        enum CCLASS
        {   
            Numeric
        }

        static Regex NumericCompare = new Regex(@"^\p{N}$", RegexOptions.Compiled);

        static bool is_cclass(string target, int index, CCLASS cclass)
        {
            switch ((int)cclass)
            {
                case 0:
                    return NumericCompare.IsMatch(target, index);
                default:
                    throw new NotImplementedException("The character class " + cclass + " is not yet implemented");
            }
        }
    }
}

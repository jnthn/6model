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
        /// Gets a value at a given key from a low level mapping (something that
        /// uses the P6mapping representation).
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="LLMapping"></param>
        /// <param name="Key"></param>
        /// <returns></returns>
        public static RakudoObject llmapping_get_at_key(ThreadContext TC, RakudoObject LLMapping, RakudoObject Key)
        {
            if (LLMapping is P6mapping.Instance)
            {
                var Storage = ((P6mapping.Instance)LLMapping).Storage;
                var StrKey = Ops.unbox_str(TC, Key);
                if (Storage.ContainsKey(StrKey))
                    return Storage[StrKey];
                else
                    return null;
            }
            else
            {
                throw new Exception("Cannot use llmapping_get_at_key if representation is not P6mapping");
            }
        }

        /// <summary>
        /// Binds a value at a given key from a low level mapping (something that
        /// uses the P6mapping representation).
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="LLMapping"></param>
        /// <param name="Key"></param>
        /// <param name="Value"></param>
        /// <returns></returns>
        public static RakudoObject llmapping_bind_at_key(ThreadContext TC, RakudoObject LLMapping, RakudoObject Key, RakudoObject Value)
        {
            if (LLMapping is P6mapping.Instance)
            {
                var Storage = ((P6mapping.Instance)LLMapping).Storage;
                var StrKey = Ops.unbox_str(TC, Key);
                if (Storage.ContainsKey(StrKey))
                    Storage[StrKey] = Value;
                else
                    Storage.Add(StrKey, Value);
                return Value;
            }
            else
            {
                throw new Exception("Cannot use llmapping_bind_at_key if representation is not P6mapping");
            }
        }

        /// <summary>
        /// Gets the number of elements in a low level mapping (something that
        /// uses the P6mapping representation).
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="LLMapping"></param>
        /// <returns></returns>
        public static RakudoObject llmapping_elems(ThreadContext TC, RakudoObject LLMapping)
        {
            if (LLMapping is P6mapping.Instance)
            {
                return Ops.box_int(TC, ((P6mapping.Instance)LLMapping).Storage.Count, TC.DefaultIntBoxType);
            }
            else
            {
                throw new Exception("Cannot use llmapping_elems if representation is not P6mapping");
            }
        }
    }
}

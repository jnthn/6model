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
        /// Gets a value at a given positional index from a native capture
        /// (something that uses the P6capture representation).
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="LLList"></param>
        /// <param name="Index"></param>
        /// <returns></returns>
        public static RakudoObject llcap_get_at_pos(ThreadContext TC, RakudoObject Capture, RakudoObject Index)
        {
            P6capture.Instance Cap;
            if ((Cap = Capture as P6capture.Instance) != null)
            {
                if (Cap.Positionals == null)
                {
                    Cap.Positionals = new RakudoObject[Ops.unbox_int(TC, Index)];
                    return Ops.get_lex(TC, "Mu");
                }
                return Cap.Positionals[Ops.unbox_int(TC, Index)] ?? Ops.get_lex(TC, "Mu");
            }
            else
            {
                throw new Exception("Cannot use llcap_get_at_pos if representation is not P6capture");
            }
        }

        /// <summary>
        /// Binds a value at a given positional index from a native capture
        /// (something that uses the P6capture representation).
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="LLList"></param>
        /// <param name="Index"></param>
        /// <returns></returns>
        public static RakudoObject llcap_bind_at_pos(ThreadContext TC, RakudoObject Capture, RakudoObject IndexObj, RakudoObject Value)
        {
            P6capture.Instance Cap;
            if ((Cap = Capture as P6capture.Instance) != null)
            {
                var Storage = Cap.Positionals;
                var Index = Ops.unbox_int(TC, IndexObj);
                if (Storage == null)
                    Storage = Cap.Positionals = new RakudoObject[Index + 1];
                if (Index >= Storage.Length)
                {
                    // XXX Need some more efficient resizable array approach...
                    // Also this is no way thread safe.
                    var newStorage = new RakudoObject[Index + 1];
                    Storage.CopyTo(newStorage, 0);
                    Cap.Positionals = newStorage;
                }
                return Storage[Index] = Value;
            }
            else
            {
                throw new Exception("Cannot use llcap_bind_at_pos if representation is not P6capture");
            }
        }

        /// <summary>
        /// Gets a value at a given key from a native capture (something that
        /// uses the P6capture representation).
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="Capture"></param>
        /// <param name="Key"></param>
        /// <returns></returns>
        public static RakudoObject llcap_get_at_key(ThreadContext TC, RakudoObject Capture, RakudoObject Key)
        {
            if (Capture is P6capture.Instance)
            {
                var Storage = ((P6capture.Instance)Capture).Nameds;
                if (Storage == null)
                    Storage = ((P6capture.Instance)Capture).Nameds = new Dictionary<string, RakudoObject>();
                var StrKey = Ops.unbox_str(TC, Key);
                if (Storage.ContainsKey(StrKey))
                    return Storage[StrKey];
                else
                    return Ops.get_lex(TC, "Mu");
            }
            else
            {
                throw new Exception("Cannot use llcap_get_at_key if representation is not P6capture");
            }
        }

        /// <summary>
        /// Binds a value at a given key from a native capture (something that
        /// uses the P6capture representation).
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="Capture"></param>
        /// <param name="Key"></param>
        /// <param name="Value"></param>
        /// <returns></returns>
        public static RakudoObject llcap_bind_at_key(ThreadContext TC, RakudoObject Capture, RakudoObject Key, RakudoObject Value)
        {
            if (Capture is P6capture.Instance)
            {
                var Storage = ((P6capture.Instance)Capture).Nameds;
                if (Storage == null)
                    Storage = ((P6capture.Instance)Capture).Nameds = new Dictionary<string, RakudoObject>();
                var StrKey = Ops.unbox_str(TC, Key);
                if (Storage.ContainsKey(StrKey))
                    Storage[StrKey] = Value;
                else
                    Storage.Add(StrKey, Value);
                return Value;
            }
            else
            {
                throw new Exception("Cannot use llcap_bind_at_key if representation is not P6capture");
            }
        }

    }
}

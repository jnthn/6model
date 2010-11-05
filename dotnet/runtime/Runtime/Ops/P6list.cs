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
        /// Gets a value at a given positional index from a low level list
        /// (something that uses the P6list representation).
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="LLList"></param>
        /// <param name="Index"></param>
        /// <returns></returns>
        public static RakudoObject lllist_get_at_pos(ThreadContext TC, RakudoObject LLList, RakudoObject Index)
        {
            if (LLList is P6list.Instance)
            {
                return ((P6list.Instance)LLList).Storage[Ops.unbox_int(TC, Index)];
            }
            else
            {
                throw new Exception("Cannot use lllist_get_at_pos if representation is not P6list");
            }
        }

        /// <summary>
        /// Binds a value at a given positional index from a low level list
        /// (something that uses the P6list representation).
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="LLList"></param>
        /// <param name="Index"></param>
        /// <returns></returns>
        public static RakudoObject lllist_bind_at_pos(ThreadContext TC, RakudoObject LLList, RakudoObject IndexObj, RakudoObject Value)
        {
            if (LLList is P6list.Instance)
            {
                var Storage = ((P6list.Instance)LLList).Storage;
                var Index = Ops.unbox_int(TC, IndexObj);
                if (Index < Storage.Count)
                {
                    Storage[Index] = Value;
                }
                else
                {
                    // XXX Need some more efficient resizable array approach...
                    // Also this is no way thread safe.
                    while (Index > Storage.Count)
                        Storage.Add(null);
                    Storage.Add(Value);
                }
                return Value;
            }
            else
            {
                throw new Exception("Cannot use lllist_bind_at_pos if representation is not P6list");
            }
        }

        /// <summary>
        /// Gets the number of elements in a low level list (something that
        /// uses the P6list representation).
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="LLList"></param>
        /// <returns></returns>
        public static RakudoObject lllist_elems(ThreadContext TC, RakudoObject LLList)
        {
            if (LLList is P6list.Instance)
            {
                return Ops.box_int(TC, ((P6list.Instance)LLList).Storage.Count, TC.DefaultIntBoxType);
            }
            else
            {
                throw new Exception("Cannot use lllist_elems if representation is not P6list");
            }
        }

        /// <summary>
        /// Pushes a value to a low level list (something that
        /// uses the P6list representation).
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="LLList"></param>
        /// <returns></returns>
        public static RakudoObject lllist_push(ThreadContext TC, RakudoObject LLList, RakudoObject item)
        {
            if (LLList is P6list.Instance)
            {
                ((P6list.Instance)LLList).Storage.Add(item);
                return item;
            }
            else
            {
                throw new Exception("Cannot use lllist_push if representation is not P6list");
            }
        }

        /// <summary>
        /// Pops a value from a low level list (something that
        /// uses the P6list representation).
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="LLList"></param>
        /// <returns></returns>
        public static RakudoObject lllist_pop(ThreadContext TC, RakudoObject LLList)
        {
            if (LLList is P6list.Instance)
            {
                List<RakudoObject> store = ((P6list.Instance)LLList).Storage;
                int idx = store.Count - 1;
                if (idx < 0)
                {
                    throw new ArgumentOutOfRangeException("Cannot pop from an empty list");
                }
                RakudoObject item = store[idx];
                store.RemoveAt(idx);
                return item;
            }
            else
            {
                throw new Exception("Cannot use lllist_pop if representation is not P6list");
            }
        }

        /// <summary>
        /// Shifts a value from a low level list (something that
        /// uses the P6list representation).
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="LLList"></param>
        /// <returns></returns>
        public static RakudoObject lllist_shift(ThreadContext TC, RakudoObject LLList)
        {
            if (LLList is P6list.Instance)
            {
                List<RakudoObject> store = ((P6list.Instance)LLList).Storage;
                int idx = store.Count - 1;
                if (idx < 0)
                {
                    throw new ArgumentOutOfRangeException("Cannot shift from an empty list");
                }
                RakudoObject item = store[0];
                store.RemoveAt(0);
                return item;
            }
            else
            {
                throw new Exception("Cannot use lllist_shift if representation is not P6list");
            }
        }

        /// <summary>
        /// Unshifts a value to a low level list (something that
        /// uses the P6list representation).
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="LLList"></param>
        /// <returns></returns>
        public static RakudoObject lllist_unshift(ThreadContext TC, RakudoObject LLList, RakudoObject item)
        {
            if (LLList is P6list.Instance)
            {
                List<RakudoObject> store = ((P6list.Instance)LLList).Storage;
                store.Insert(0, item);
                return item;
            }
            else
            {
                throw new Exception("Cannot use lllist_unshift if representation is not P6list");
            }
        }
    }
}

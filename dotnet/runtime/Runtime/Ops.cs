﻿using System;
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
    public static class Ops
    {
        /// <summary>
        /// Creates a type object associated with the given HOW and of the
        /// given representation.
        /// </summary>
        /// <param name="HOW"></param>
        /// <param name="REPRName"></param>
        /// <returns></returns>
        public static RakudoObject type_object_for(ThreadContext TC, RakudoObject HOW, string REPRName)
        {
            return REPRRegistry.get_REPR_by_name(REPRName).type_object_for(TC, HOW);
        }

        /// <summary>
        /// Create an instance of an object.
        /// </summary>
        /// <param name="WHAT"></param>
        /// <returns></returns>
        public static RakudoObject instance_of(ThreadContext TC, RakudoObject WHAT)
        {
            return WHAT.STable.REPR.instance_of(TC, WHAT);
        }

        /// <summary>
        /// Checks if the representation considers the object defined.
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public static bool repr_defined(ThreadContext TC, RakudoObject Obj)
        {
            return Obj.STable.REPR.defined(TC, Obj);
        }

        /// <summary>
        /// Gets the value of an attribute.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Class"></param>
        /// <param name="Name"></param>
        /// <returns></returns>
        public static RakudoObject get_attr(ThreadContext TC, RakudoObject Object, RakudoObject Class, string Name)
        {
            return Object.STable.REPR.get_attribute(TC, Object, Class, Name);
        }

        /// <summary>
        /// Gets the value of an attribute, using the given hint.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Class"></param>
        /// <param name="Name"></param>
        /// <param name="Hint"></param>
        /// <returns></returns>
        public static RakudoObject get_attr_with_hint(ThreadContext TC, RakudoObject Object, RakudoObject Class, string Name, int Hint)
        {
            return Object.STable.REPR.get_attribute_with_hint(TC, Object, Class, Name, Hint);
        }

        /// <summary>
        /// Binds the value of an attribute to the given value.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Class"></param>
        /// <param name="Name"></param>
        public static RakudoObject bind_attr(ThreadContext TC, RakudoObject Object, RakudoObject Class, string Name, RakudoObject Value)
        {
            Object.STable.REPR.bind_attribute(TC, Object, Class, Name, Value);
            return Value;
        }

        /// <summary>
        /// Binds the value of an attribute to the given value, using the
        /// given hint.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Class"></param>
        /// <param name="Name"></param>
        /// <param name="Hint"></param>
        public static RakudoObject bind_attr_with_hint(ThreadContext TC, RakudoObject Object, RakudoObject Class, string Name, int Hint, RakudoObject Value)
        {
            Object.STable.REPR.bind_attribute_with_hint(TC, Object, Class, Name, Hint, Value);
            return Value;
        }

        /// <summary>
        /// Finds a method to call by name.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Name"></param>
        /// <returns></returns>
        public static RakudoObject find_method(ThreadContext TC, RakudoObject Object, string Name)
        {
            return Object.STable.FindMethod(TC, Object, Name, Hints.NO_HINT);
        }

        /// <summary>
        /// Finds a method to call, using the hint if available.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Name"></param>
        /// <param name="Hint"></param>
        /// <returns></returns>
        public static RakudoObject find_method_with_hint(ThreadContext TC, RakudoObject Object, string Name, int Hint)
        {
            return Object.STable.FindMethod(TC, Object, Name, Hint);
        }

        /// <summary>
        /// Invokes the given method.
        /// </summary>
        /// <param name="Invokee"></param>
        /// <param name="Capture"></param>
        /// <returns></returns>
        public static RakudoObject invoke(ThreadContext TC, RakudoObject Invokee, RakudoObject Capture)
        {
            return Invokee.STable.Invoke(TC, Invokee, Capture);
        }

        /// <summary>
        /// Gets the HOW (higher order workings, e.g. meta-package).
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public static RakudoObject get_how(ThreadContext TC, RakudoObject Obj)
        {
            return Obj.STable.HOW;
        }

        /// <summary>
        /// Gets the WHAT (type object).
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public static RakudoObject get_what(ThreadContext TC, RakudoObject Obj)
        {
            return Obj.STable.WHAT;
        }

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

        /// <summary>
        /// Gets a lexical variable of the given name.
        /// </summary>
        /// <param name="i"></param>
        /// <param name="name"></param>
        /// <returns></returns>
        public static RakudoObject get_lex(ThreadContext TC, string Name)
        {
            var CurContext = TC.CurrentContext;
            while (CurContext != null)
            {
                int Index;
                if (CurContext.LexPad.SlotMapping.TryGetValue(Name, out Index))
                    return CurContext.LexPad.Storage[Index];
                CurContext = CurContext.Outer;
            }
            throw new InvalidOperationException("No variable " + Name + " found in the lexical scope");
        }

        /// <summary>
        /// Binds the given value to a lexical variable of the given name.
        /// </summary>
        /// <param name="i"></param>
        /// <param name="name"></param>
        /// <returns></returns>
        public static RakudoObject bind_lex(ThreadContext TC, string Name, RakudoObject Value)
        {
            var CurContext = TC.CurrentContext;
            while (CurContext != null)
            {
                int Index;
                if (CurContext.LexPad.SlotMapping.TryGetValue(Name, out Index))
                {
                    CurContext.LexPad.Storage[Index] = Value;
                    return Value;
                }
                CurContext = CurContext.Outer;
            }
            throw new InvalidOperationException("No variable " + Name + " found in the lexical scope");
        }

        /// <summary>
        /// Looks up a variable in the dynamic scope.
        /// </summary>
        /// <param name="C"></param>
        /// <param name="Name"></param>
        /// <returns></returns>
        public static RakudoObject get_dynamic(ThreadContext TC, string Name)
        {
            var CurContext = TC.CurrentContext;
            while (CurContext != null)
            {
                int Index;
                if (CurContext.LexPad.SlotMapping.TryGetValue(Name, out Index))
                    return CurContext.LexPad.Storage[Index];
                CurContext = CurContext.Caller;
            }
            throw new InvalidOperationException("No variable " + Name + " found in the dynamic scope");
        }

        /// <summary>
        /// Binds the given value to a variable in the dynamic scope.
        /// </summary>
        /// <param name="C"></param>
        /// <param name="Name"></param>
        /// <returns></returns>
        public static RakudoObject bind_dynamic(ThreadContext TC, string Name, RakudoObject Value)
        {
            var CurContext = TC.CurrentContext;
            while (CurContext != null)
            {
                int Index;
                if (CurContext.LexPad.SlotMapping.TryGetValue(Name, out Index))
                {
                    CurContext.LexPad.Storage[Index] = Value;
                    return Value;
                }
                CurContext = CurContext.Caller;
            }
            throw new InvalidOperationException("No variable " + Name + " found in the dynamic scope");
        }

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

        /// <summary>
        /// Entry point to multi-dispatch over the candidates in the inner
        /// dispatcher.
        /// </summary>
        /// <param name="TC"></param>
        /// <returns></returns>
        public static RakudoObject multi_dispatch_over_lexical_candidates(ThreadContext TC, RakudoObject Name)
        {
            var Candidate = MultiDispatch.MultiDispatcher.FindBestCandidate(
                MultiDispatch.LexicalCandidateFinder.FindCandidates(
                    TC.CurrentContext.Caller,
                    TC.CurrentContext.Outer,
                    "!" + Ops.unbox_str(TC, Name) + "-candidates"),
                TC.CurrentContext.Capture);
            return Candidate.STable.Invoke(TC, Candidate, TC.CurrentContext.Capture);
        }

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

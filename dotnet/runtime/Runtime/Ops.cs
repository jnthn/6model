using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;

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
        public static RakudoObject type_object_for(RakudoObject HOW, string REPRName)
        {
            return REPRRegistry.get_REPR_by_name(REPRName).type_object_for(HOW);
        }

        /// <summary>
        /// Create an instance of an object.
        /// </summary>
        /// <param name="WHAT"></param>
        /// <returns></returns>
        public static RakudoObject instance_of(RakudoObject WHAT)
        {
            return WHAT.STable.REPR.instance_of(WHAT);
        }

        /// <summary>
        /// Checks if the representation considers the object defined.
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public static bool repr_defined(RakudoObject Obj)
        {
            return Obj.STable.REPR.defined(Obj);
        }

        /// <summary>
        /// Gets the value of an attribute.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Class"></param>
        /// <param name="Name"></param>
        /// <returns></returns>
        public static RakudoObject get_attr(RakudoObject Object, RakudoObject Class, string Name)
        {
            return Object.STable.REPR.get_attribute(Object, Class, Name);
        }

        /// <summary>
        /// Gets the value of an attribute, using the given hint.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Class"></param>
        /// <param name="Name"></param>
        /// <param name="Hint"></param>
        /// <returns></returns>
        public static RakudoObject get_attr_with_hint(RakudoObject Object, RakudoObject Class, string Name, int Hint)
        {
            return Object.STable.REPR.get_attribute_with_hint(Object, Class, Name, Hint);
        }

        /// <summary>
        /// Binds the value of an attribute to the given value.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Class"></param>
        /// <param name="Name"></param>
        /// <param name="Hint"></param>
        public static void bind_attr_with_hint(RakudoObject Object, RakudoObject Class, string Name, RakudoObject Value)
        {
            Object.STable.REPR.bind_attribute(Object, Class, Name, Value);
        }

        /// <summary>
        /// Binds the value of an attribute to the given value, using the
        /// given hint.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Class"></param>
        /// <param name="Name"></param>
        /// <param name="Hint"></param>
        public static void bind_attr_with_hint(RakudoObject Object, RakudoObject Class, string Name, int Hint, RakudoObject Value)
        {
            Object.STable.REPR.bind_attribute_with_hint(Object, Class, Name, Hint, Value);
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
        public static RakudoObject get_how(RakudoObject Obj)
        {
            return Obj.STable.HOW;
        }

        /// <summary>
        /// Gets the WHAT (type object).
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public static RakudoObject get_what(RakudoObject Obj)
        {
            return Obj.STable.WHAT;
        }

        /// <summary>
        /// Boxes a native type into its matching value type.
        /// </summary>
        /// <param name="Value"></param>
        /// <returns></returns>
        public static RakudoObject box<TValue>(TValue Value, RakudoObject To)
        {
            if (To.STable.REPR is IBoxableRepresentation<TValue>)
            {
                var Result = To.STable.REPR.instance_of(To);
                (To.STable.REPR as IBoxableRepresentation<TValue>).set_value(Result, Value);
                return Result;
            }
            else
            {
                throw new Exception("Can not box an " + typeof(TValue).Name + " to an object of unsuitable representation.");
            }
        }

        /// <summary>
        /// Unboxes a value.
        /// </summary>
        /// <param name="Boxed"></param>
        /// <returns></returns>
        public static TValue unbox<TValue>(RakudoObject Boxed)
        {
            if (Boxed.STable.REPR is IBoxableRepresentation<TValue>)
                return (Boxed.STable.REPR as IBoxableRepresentation<TValue>).get_value(Boxed);
            else
                throw new Exception("Can not unbox an " + typeof(TValue).Name + " from an object of unsuitable representation.");
        }

        /// <summary>
        /// Coerces an integer into a string.
        /// </summary>
        /// <param name="Int"></param>
        /// <param name="TargetType"></param>
        /// <returns></returns>
        public static RakudoObject coerce_int_to_str(RakudoObject Int, RakudoObject TargetType)
        {
            int Value = Ops.unbox<int>(Int);
            return Ops.box(Value.ToString(), TargetType);
        }

        /// <summary>
        /// Coerces a floating point number into a string.
        /// </summary>
        /// <param name="Num"></param>
        /// <param name="TargetType"></param>
        /// <returns></returns>
        public static RakudoObject coerce_num_to_str(RakudoObject Num, RakudoObject TargetType)
        {
            double Value = Ops.unbox<double>(Num);
            return Ops.box(Value.ToString(), TargetType);
        }

        /// <summary>
        /// Coerces an integer into a floating point number.
        /// </summary>
        /// <param name="Int"></param>
        /// <param name="TargetType"></param>
        /// <returns></returns>
        public static RakudoObject coerce_int_to_num(RakudoObject Int, RakudoObject TargetType)
        {
            int Value = Ops.unbox<int>(Int);
            return Ops.box((double)Value, TargetType);
        }

        /// <summary>
        /// Coerces a floating point number into an integer.
        /// </summary>
        /// <param name="Int"></param>
        /// <param name="TargetType"></param>
        /// <returns></returns>
        public static RakudoObject coerce_num_to_int(RakudoObject Num, RakudoObject TargetType)
        {
            double Value = Ops.unbox<double>(Num);
            return Ops.box((int)Value, TargetType);
        }

        /// <summary>
        /// Gets a lexical variable of the given name.
        /// </summary>
        /// <param name="i"></param>
        /// <param name="name"></param>
        /// <returns></returns>
        public static RakudoObject get_lex(Context C, string Name)
        {
            var CurContext = C;
            while (CurContext != null)
            {
                RakudoObject Found;
                if (CurContext.LexPad.TryGetValue(Name, out Found))
                    return Found;
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
        public static RakudoObject bind_lex(Context C, string Name, RakudoObject Value)
        {
            var CurContext = C;
            while (CurContext != null)
            {
                if (CurContext.LexPad.ContainsKey(Name))
                {
                    CurContext.LexPad[Name] = Value;
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
        public static RakudoObject get_dynamic(Context C, string Name)
        {
            var CurContext = C;
            while (CurContext != null)
            {
                RakudoObject Found;
                if (CurContext.LexPad.TryGetValue(Name, out Found))
                    return Found;
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
        public static RakudoObject bind_dynamic(Context C, string Name, RakudoObject Value)
        {
            var CurContext = C;
            while (CurContext != null)
            {
                if (CurContext.LexPad.ContainsKey(Name))
                {
                    CurContext.LexPad[Name] = Value;
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
        public static RakudoObject equal_nums(RakudoObject x, RakudoObject y, RakudoObject ResultType)
        {
            return Ops.box<int>(
                (Ops.unbox<double>(x) == Ops.unbox<double>(y) ? 1 : 0),
                ResultType);
        }

        /// <summary>
        /// Compares two integers for equality.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject equal_ints(RakudoObject x, RakudoObject y, RakudoObject ResultType)
        {
            return Ops.box<int>(
                (Ops.unbox<int>(x) == Ops.unbox<int>(y) ? 1 : 0),
                ResultType);
        }

        /// <summary>
        /// Compares two strings for equality.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject equal_strs(RakudoObject x, RakudoObject y, RakudoObject ResultType)
        {
            return Ops.box<int>(
                (Ops.unbox<string>(x) == Ops.unbox<string>(y) ? 1 : 0),
                ResultType);
        }

        /// <summary>
        /// Logical not.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject logical_not_int(RakudoObject x, RakudoObject ResultType)
        {
            return Ops.box<int>(Ops.unbox<int>(x) == 0 ? 1 : 0, ResultType);
        }

        /// <summary>
        /// Performs an integer addition.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject add_int(RakudoObject x, RakudoObject y, RakudoObject ResultType)
        {
            return Ops.box<int>(Ops.unbox<int>(x) + Ops.unbox<int>(y), ResultType);
        }

        /// <summary>
        /// Performs an integer subtraction.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject sub_int(RakudoObject x, RakudoObject y, RakudoObject ResultType)
        {
            return Ops.box<int>(Ops.unbox<int>(x) - Ops.unbox<int>(y), ResultType);
        }

        /// <summary>
        /// Performs an integer multiplication.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject mul_int(RakudoObject x, RakudoObject y, RakudoObject ResultType)
        {
            return Ops.box<int>(Ops.unbox<int>(x) * Ops.unbox<int>(y), ResultType);
        }

        /// <summary>
        /// Performs an integer division.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject div_int(RakudoObject x, RakudoObject y, RakudoObject ResultType)
        {
            return Ops.box<int>(Ops.unbox<int>(x) / Ops.unbox<int>(y), ResultType);
        }

        /// <summary>
        /// Performs an integer modulo.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject mod_int(RakudoObject x, RakudoObject y, RakudoObject ResultType)
        {
            return Ops.box<int>(Ops.unbox<int>(x) % Ops.unbox<int>(y), ResultType);
        }

        /// <summary>
        /// Performs a string concatenation.
        /// </summary>
        /// <param name="x"></param>
        /// <param name="y"></param>
        /// <param name="ResultType"></param>
        /// <returns></returns>
        public static RakudoObject concat(RakudoObject x, RakudoObject y, RakudoObject ResultType)
        {
            return Ops.box<string>(Ops.unbox<string>(x) + Ops.unbox<string>(y), ResultType);
        }
    }
}

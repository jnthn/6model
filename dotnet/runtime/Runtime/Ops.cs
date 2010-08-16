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
        public static IRakudoObject type_object_for(IRakudoObject HOW, string REPRName)
        {
            return REPRRegistry.get_REPR_by_name(REPRName).type_object_for(HOW);
        }

        /// <summary>
        /// Create an instance of an object.
        /// </summary>
        /// <param name="WHAT"></param>
        /// <returns></returns>
        public static IRakudoObject instance_of(IRakudoObject WHAT)
        {
            return WHAT.STable.REPR.instance_of(WHAT);
        }

        /// <summary>
        /// Checks if the representation considers the object defined.
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public static bool repr_defined(IRakudoObject Obj)
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
        public static IRakudoObject get_attr(IRakudoObject Object, IRakudoObject Class, string Name)
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
        public static IRakudoObject get_attr_with_hint(IRakudoObject Object, IRakudoObject Class, string Name, int Hint)
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
        public static void bind_attr_with_hint(IRakudoObject Object, IRakudoObject Class, string Name, IRakudoObject Value)
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
        public static void bind_attr_with_hint(IRakudoObject Object, IRakudoObject Class, string Name, int Hint, IRakudoObject Value)
        {
            Object.STable.REPR.bind_attribute_with_hint(Object, Class, Name, Hint, Value);
        }

        /// <summary>
        /// Finds a method to call by name.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Name"></param>
        /// <returns></returns>
        public static IRakudoObject find_method(ThreadContext TC, IRakudoObject Object, string Name)
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
        public static IRakudoObject find_method_with_hint(ThreadContext TC, IRakudoObject Object, string Name, int Hint)
        {
            return Object.STable.FindMethod(TC, Object, Name, Hint);
        }

        /// <summary>
        /// Invokes the given method.
        /// </summary>
        /// <param name="Invokee"></param>
        /// <param name="Capture"></param>
        /// <returns></returns>
        public static IRakudoObject invoke(ThreadContext TC, IRakudoObject Invokee, IRakudoObject Capture)
        {
            return Invokee.STable.Invoke(TC, Invokee, Capture);
        }

        /// <summary>
        /// Gets the HOW (higher order workings, e.g. meta-package).
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public static IRakudoObject get_how(IRakudoObject Obj)
        {
            return Obj.STable.HOW;
        }

        /// <summary>
        /// Gets the WHAT (type object).
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public static IRakudoObject get_what(IRakudoObject Obj)
        {
            return Obj.STable.WHAT;
        }

        /// <summary>
        /// Boxes a native type into its matching value type.
        /// </summary>
        /// <param name="Value"></param>
        /// <returns></returns>
        public static IRakudoObject box<TValue>(TValue Value, IRakudoObject To)
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
        public static TValue unbox<TValue>(IRakudoObject Boxed)
        {
            if (Boxed.STable.REPR is IBoxableRepresentation<TValue>)
                return (Boxed.STable.REPR as IBoxableRepresentation<TValue>).get_value(Boxed);
            else
                throw new Exception("Can not unbox an " + typeof(TValue).Name + " from an object of unsuitable representation.");
        }

        /// <summary>
        /// Gets a lexical variable of the given name.
        /// </summary>
        /// <param name="i"></param>
        /// <param name="name"></param>
        /// <returns></returns>
        public static IRakudoObject get_lex(Context C, string Name)
        {
            var CurContext = C;
            while (CurContext != null)
            {
                if (CurContext.LexPad.ContainsKey(Name))
                    return CurContext.LexPad[Name];
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
        public static IRakudoObject bind_lex(Context C, string Name, IRakudoObject Value)
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
        public static IRakudoObject get_dynamic(Context C, string Name)
        {
            var CurContext = C;
            while (CurContext != null)
            {
                if (CurContext.LexPad.ContainsKey(Name))
                    return CurContext.LexPad[Name];
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
        public static IRakudoObject bind_dynamic(Context C, string Name, IRakudoObject Value)
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
    }
}

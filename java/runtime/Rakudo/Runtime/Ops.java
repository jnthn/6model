package Rakudo.Runtime;

import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.REPRRegistry;
import Rakudo.Metamodel.SharedTable;

/// <summary>
/// This class implements the various vm::op options that are
/// available.
/// </summary>
public class Ops
{
    /// <summary>
    /// Creates a type object associated with the given HOW and of the
    /// given representation.
    /// </summary>
    /// <param name="HOW"></param>
    /// <param name="REPRName"></param>
    /// <returns></returns>
    public static RakudoObject type_object_for(ThreadContext TC, RakudoObject HOW, String REPRName)
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
        return WHAT.getSTable().REPR.instance_of(TC, WHAT);
    }

    /// <summary>
    /// Checks if the representation considers the object defined.
    /// </summary>
    /// <param name="Obj"></param>
    /// <returns></returns>
    public static boolean repr_defined(ThreadContext TC, RakudoObject Obj)
    {
        return Obj.getSTable().REPR.defined(TC, Obj);
    }

    /// <summary>
    /// Gets the value of an attribute.
    /// </summary>
    /// <param name="Object"></param>
    /// <param name="Class"></param>
    /// <param name="Name"></param>
    /// <returns></returns>
    public static RakudoObject get_attr(ThreadContext TC, RakudoObject Object, RakudoObject Class, String Name)
    {
        return Object.getSTable().REPR.get_attribute(TC, Object, Class, Name);
    }

    /// <summary>
    /// Gets the value of an attribute, using the given hint.
    /// </summary>
    /// <param name="Object"></param>
    /// <param name="Class"></param>
    /// <param name="Name"></param>
    /// <param name="Hint"></param>
    /// <returns></returns>
    public static RakudoObject get_attr_with_hint(ThreadContext TC, RakudoObject Object, RakudoObject Class, String Name, int Hint)
    {
        return Object.getSTable().REPR.get_attribute_with_hint(TC, Object, Class, Name, Hint);
    }

    /// <summary>
    /// Binds the value of an attribute to the given value.
    /// </summary>
    /// <param name="Object"></param>
    /// <param name="Class"></param>
    /// <param name="Name"></param>
    /// <param name="Hint"></param>
    public static void bind_attr_with_hint(ThreadContext TC, RakudoObject Object, RakudoObject Class, String Name, RakudoObject Value)
    {
        Object.getSTable().REPR.bind_attribute(TC, Object, Class, Name, Value);
    }

    /// <summary>
    /// Binds the value of an attribute to the given value, using the
    /// given hint.
    /// </summary>
    /// <param name="Object"></param>
    /// <param name="Class"></param>
    /// <param name="Name"></param>
    /// <param name="Hint"></param>
    public static void bind_attr_with_hint(ThreadContext TC, RakudoObject Object, RakudoObject Class, String Name, int Hint, RakudoObject Value)
    {
        Object.getSTable().REPR.bind_attribute_with_hint(TC, Object, Class, Name, Hint, Value);
    }

    /// <summary>
    /// Finds a method to call by name.
    /// </summary>
    /// <param name="Object"></param>
    /// <param name="Name"></param>
    /// <returns></returns>
    public static RakudoObject find_method(ThreadContext TC, RakudoObject Object, String Name)
    {
// TODO return Object.getSTable().FindMethod.FindMethod.Invoke.Invoke(TC, Object, Name, Hints.NO_HINT);
        return null;
    }

    /// <summary>
    /// Finds a method to call, using the hint if available.
    /// </summary>
    /// <param name="Object"></param>
    /// <param name="Name"></param>
    /// <param name="Hint"></param>
    /// <returns></returns>
    public static RakudoObject find_method_with_hint(ThreadContext TC, RakudoObject Object, String Name, int Hint)
    {
// TODO return Object.getSTable().FindMethod(TC, Object, Name, Hint);
        return null;
    }

    /// <summary>
    /// Invokes the given method.
    /// </summary>
    /// <param name="Invokee"></param>
    /// <param name="Capture"></param>
    /// <returns></returns>
    public static RakudoObject invoke(ThreadContext TC, RakudoObject Invokee, RakudoObject Capture)
    {
        return Invokee.getSTable().Invoke.Invoke(TC, Invokee, Capture);
    }

    /// <summary>
    /// Gets the HOW (higher order workings, e.g. meta-package).
    /// </summary>
    /// <param name="Obj"></param>
    /// <returns></returns>
    public static RakudoObject get_how(ThreadContext TC, RakudoObject Obj)
    {
        return Obj.getSTable().HOW;
    }

    /// <summary>
    /// Gets the WHAT (type object).
    /// </summary>
    /// <param name="Obj"></param>
    /// <returns></returns>
    public static RakudoObject get_what(ThreadContext TC, RakudoObject Obj)
    {
        return Obj.getSTable().WHAT;
    }

    /// <summary>
    /// Boxes a native int into its matching value type.
    /// </summary>
    /// <param name="Value"></param>
    /// <returns></returns>
    public static RakudoObject box_int(ThreadContext TC, int Value, RakudoObject To)
    {
        Representation REPR = To.getSTable().REPR;
        RakudoObject Result = REPR.instance_of(TC, To);
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
        Representation REPR = To.getSTable().REPR;
        RakudoObject Result = REPR.instance_of(TC, To);
        REPR.set_num(TC, Result, Value);
        return Result;
    }

    /// <summary>
    /// Boxes a native string into its matching value type.
    /// </summary>
    /// <param name="Value"></param>
    /// <returns></returns>
    public static RakudoObject box_str(ThreadContext TC, String Value, RakudoObject To)
    {
        Representation REPR = To.getSTable().REPR;
        RakudoObject Result = REPR.instance_of(TC, To);
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
        return Boxed.getSTable().REPR.get_int(TC, Boxed);
    }

    /// <summary>
    /// Unboxes a boxed num.
    /// </summary>
    /// <param name="Boxed"></param>
    /// <returns></returns>
    public static double unbox_num(ThreadContext TC, RakudoObject Boxed)
    {
        return Boxed.getSTable().REPR.get_num(TC, Boxed);
    }

    /// <summary>
    /// Unboxes a boxed string.
    /// </summary>
    /// <param name="Boxed"></param>
    /// <returns></returns>
    public static String unbox_str(ThreadContext TC, RakudoObject Boxed)
    {
        return Boxed.getSTable().REPR.get_str(TC, Boxed);
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
        return Ops.box_str(TC, Integer.toString(Value), TargetType);
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
        return Ops.box_str(TC, Double.toString(Value), TargetType);
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
    /// Gets a lexical variable of the given name.
    /// </summary>
    /// <param name="i"></param>
    /// <param name="name"></param>
    /// <returns></returns>
    public static RakudoObject get_lex(ThreadContext TC, String Name)
    {
        Context CurContext = TC.CurrentContext;
        while (CurContext != null)
        {
            // if (CurContext.LexPad.SlotMapping.TryGetValue(Name, out Index)) // the C# version
            if (CurContext.LexPad.SlotMapping.containsKey(Name)) {
                int Index;
                Index = CurContext.LexPad.SlotMapping.get(Name);
                return CurContext.LexPad.Storage[Index];
            }
            CurContext = CurContext.Outer;
        }
        throw new UnsupportedOperationException("No variable " + Name + " found in the lexical scope");
    }

    /// <summary>
    /// Binds the given value to a lexical variable of the given name.
    /// </summary>
    /// <param name="i"></param>
    /// <param name="name"></param>
    /// <returns></returns>
    public static RakudoObject bind_lex(ThreadContext TC, String Name, RakudoObject Value)
    {
        Context CurContext = TC.CurrentContext;
        while (CurContext != null)
        {
            // if (CurContext.LexPad.SlotMapping.TryGetValue(Name, out Index)) // the C# version
            if (CurContext.LexPad.SlotMapping.containsKey(Name))
            {
                int Index;
                Index = CurContext.LexPad.SlotMapping.get(Name);
                CurContext.LexPad.Storage[Index] = Value;
                return Value;
            }
            CurContext = CurContext.Outer;
        }
        throw new UnsupportedOperationException("No variable " + Name + " found in the lexical scope");
    }

    /// <summary>
    /// Looks up a variable in the dynamic scope.
    /// </summary>
    /// <param name="C"></param>
    /// <param name="Name"></param>
    /// <returns></returns>
    public static RakudoObject get_dynamic(ThreadContext TC, String Name)
    {
        Context CurContext = TC.CurrentContext;
        while (CurContext != null)
        {
            // if (CurContext.LexPad.SlotMapping.TryGetValue(Name, out Index)) // the C# version
            if (CurContext.LexPad.SlotMapping.containsKey(Name)) {
                int Index;
                Index = CurContext.LexPad.SlotMapping.get(Name);
                return CurContext.LexPad.Storage[Index];
            }
            CurContext = CurContext.Caller;
        }
        throw new UnsupportedOperationException("No variable " + Name + " found in the dynamic scope");
    }

    /// <summary>
    /// Binds the given value to a variable in the dynamic scope.
    /// </summary>
    /// <param name="C"></param>
    /// <param name="Name"></param>
    /// <returns></returns>
    public static RakudoObject bind_dynamic(ThreadContext TC, String Name, RakudoObject Value)
    {
        Context CurContext = TC.CurrentContext;
        while (CurContext != null)
        {
            // if (CurContext.LexPad.SlotMapping.TryGetValue(Name, out Index)) // the C# version
            if (CurContext.LexPad.SlotMapping.containsKey(Name))
            {
                int Index;
                Index = CurContext.LexPad.SlotMapping.get(Name);
                CurContext.LexPad.Storage[Index] = Value;
                return Value;
            }
            CurContext = CurContext.Caller;
        }
        throw new UnsupportedOperationException("No variable " + Name + " found in the dynamic scope");
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
/* TODO
        var Candidate = MultiDispatch.MultiDispatcher.FindBestCandidate(
            MultiDispatch.LexicalCandidateFinder.FindCandidates(
                TC.CurrentContext.Caller,
                TC.CurrentContext.Outer,
                "!" + Ops.unbox_str(TC, Name) + "-candidates"),
            TC.CurrentContext.Capture);
        return Candidate.getSTable().Invoke(TC, Candidate, TC.CurrentContext.Capture);
*/
        return null; // TODO remove
    }
}

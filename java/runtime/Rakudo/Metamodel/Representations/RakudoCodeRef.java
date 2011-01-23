package Rakudo.Metamodel.Representations;

import java.util.HashMap;

import Rakudo.Runtime.Context;
import Rakudo.Runtime.Exceptions.Handler;
import Rakudo.Runtime.ThreadContext;
import Rakudo.Runtime.Lexpad;
import Rakudo.Runtime.MultiDispatch.DispatchCache;
import Rakudo.Runtime.Signature;
import Rakudo.Metamodel.SharedTable;
import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Serialization.SerializationContext;

/// <summary>
/// A representation for a low-level code object (something that actually
/// references a piece of code that we'll run). This is used for things
/// that serve the role of an only sub (that has a body) and a dispatcher
/// (which has a body as well as a list of candidates that it operates
/// on).
/// </summary>
public final class RakudoCodeRef implements Representation // C# has public sealed class
{
    // Interface for the purpose of constructing anonymous classes that
    // implement it, as the Java equivalent of a C# lambda expression.
    // Used in for example: CodeObjectUtility
    public interface IFunc_Body {
        public RakudoObject Invoke(ThreadContext tc, RakudoObject meth, RakudoObject capt);
    }  // C# has public Func<ThreadContext, RakudoObject, RakudoObject, RakudoObject>;

    /// <summary>
    /// Instance that uses the RakudoCodeRef representation.
    /// </summary>
    public final class Instance extends RakudoObject // C# has public sealed class
    {
        /// <summary>
        /// The code body - the thing that actually runs instructions.
        /// </summary>
        public RakudoCodeRef.IFunc_Body Body; // see above IFunc_Body interface explanation

        /// <summary>
        /// The static lexpad.
        /// </summary>
        public Lexpad StaticLexPad;
        
        /// <summary>
        /// Our static outer block.
        /// </summary>
        public Instance OuterBlock;

        /// <summary>
        /// Signature object.
        /// </summary>
        public Signature Sig;

        /// <summary>
        /// Exception handlers this block has, if any.
        /// </summary>
        public Handler[] Handlers;
        
        /// <summary>
        /// If this is a dispatcher, this is the list of dispatchees that
        /// it will operate over.
        /// </summary>
        public RakudoObject[] Dispatchees;

        /// <summary>
        /// Multiple dispatch cache, if we have one.
        /// </summary>
        public DispatchCache MultiDispatchCache;

        /// <summary>
        /// The context currently using this sub.
        /// </summary>
        public Context CurrentContext;

        /// <summary>
        /// The outer context to use for the next invocation, if any.
        /// </summary>
        public Context OuterForNextInvocation;
        
        /// <summary>
        /// Creates a new instance with the given S-Table.
        /// </summary>
        /// <param name="STable"></param>
        public Instance(SharedTable sharedTable)
        {
            this.setSTable(sharedTable);
        }
    }

    /// <summary>
    /// Create a new type object.
    /// </summary>
    /// <param name="MetaPackage"></param>
    /// <returns></returns>
    public RakudoObject type_object_for(ThreadContext tc, RakudoObject metaPackage)
    {
        // Do the usual bits of setup for the type-object.
        SharedTable sharedTable = new SharedTable();
        sharedTable.HOW = metaPackage;
        sharedTable.REPR = this;
        sharedTable.WHAT = new Instance(sharedTable);

        // Also twiddle the Shared Table's Invoke to invoke the contained
        // function.
        sharedTable.Invoke = new IFunc_Body() { // C# has a lambda
            public RakudoObject Invoke(ThreadContext tci, RakudoObject methObj, RakudoObject capture)
            {
                return ((RakudoCodeRef.Instance)methObj).Body.Invoke(tci, methObj, capture);
            }
        };
        return sharedTable.WHAT;
    }

    /// <summary>
    /// Creates an instance of the type with the given type object.
    /// </summary>
    /// <param name="WHAT"></param>
    /// <returns></returns>
    public RakudoObject instance_of(ThreadContext tc, RakudoObject typeObject)
    {
        return new Instance(typeObject.getSTable());
    }

    /// <summary>
    /// Determines if the representation is defined or not.
    /// </summary>
    /// <param name="Obj"></param>
    /// <returns></returns>
    public boolean defined(ThreadContext tc, RakudoObject rakudoObject)
    {
        return ((Instance)rakudoObject).Body != null;
    }

    public RakudoObject get_attribute(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name)
    {
        throw new UnsupportedOperationException("RakudoCodeRef objects cannot store additional attributes.");
    }

    public RakudoObject get_attribute_with_hint(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint)
    {
        throw new UnsupportedOperationException("RakudoCodeRef objects cannot store additional attributes.");
    }

    public void bind_attribute(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name, RakudoObject Value)
    {
        throw new UnsupportedOperationException("RakudoCodeRef objects cannot store additional attributes.");
    }

    public void bind_attribute_with_hint(ThreadContext tc, RakudoObject Object, RakudoObject ClassHandle, String Name, int Hint, RakudoObject Value)
    {
        throw new UnsupportedOperationException("RakudoCodeRef objects cannot store additional attributes.");
    }

    public int hint_for(ThreadContext tc, RakudoObject ClassHandle, String Name)
    {
        return Hints.NO_HINT;
    }

    public void set_int(ThreadContext tc, RakudoObject Object, int Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native int");
    }

    public int get_int(ThreadContext tc, RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native int");
    }

    public void set_num(ThreadContext tc, RakudoObject Object, double Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native num");
    }

    public double get_num(ThreadContext tc, RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native num");
    }

    public void set_str(ThreadContext tc, RakudoObject Object, String Value)
    {
        throw new UnsupportedOperationException("This type of representation cannot box a native string");
    }

    public String get_str(ThreadContext tc, RakudoObject Object)
    {
        throw new UnsupportedOperationException("This type of representation cannot unbox to a native string");
    }
}


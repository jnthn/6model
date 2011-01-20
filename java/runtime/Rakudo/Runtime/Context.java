package Rakudo.Runtime;

import java.util.HashMap;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representations.RakudoCodeRef;




/// <summary>
/// A context represents a given invocation of a block. (Note this is
/// fairly sketchy at the moment.)
/// </summary>
public class Context
{
    /// <summary>
    /// The static chain.
    /// </summary>
    public Context Outer;

    /// <summary>
    /// The dynamic chain.
    /// </summary>
    public Context Caller;

    /// <summary>
    /// The static code object.
    /// </summary>
    public RakudoCodeRef.Instance StaticCodeObject;

    /// <summary>
    /// Lexpad.
    /// </summary>
    public Lexpad LexPad;

    /// <summary>
    /// The capture passed as part of the current call.
    /// </summary>
    public RakudoObject Capture;

    /// <summary>
    /// Creates an empty, uninitialized context.
    /// </summary>
    public Context() // it could be private, except that Init() calls it.
    {
        // System.err.println( "new empty Context created" );
        // this.LexPad = new Lexpad( new String[] {} ); // parameter is an empty list of strings
    }

    /// <summary>
    /// Constructor initializes the context.
    /// </summary>
    /// <param name="StaticCodeObject"></param>
    /// <param name="Caller"></param>
    public Context(RakudoObject staticCodeObject_Uncast, Context caller, RakudoObject capture)
    {
        // Set up static code object and caller pointers.
        RakudoCodeRef.Instance staticCodeObject = (RakudoCodeRef.Instance)staticCodeObject_Uncast;
        this.StaticCodeObject = staticCodeObject;
        this.Caller = caller;
        this.Capture = capture;

        // Static sub object should have this as the current
        // context.
        staticCodeObject.CurrentContext = this;
        
        this.LexPad = new Lexpad( new String[] {} ); // parameter is an empty list of strings

        // Lex pad should be an "instantiation" of the static one.
        // Instantiating a lexpad creates a new dynamic instance of it
        // from a static one, copying over the slot storage entries
        // from the static one but sharing the slot mapping.
        this.LexPad.SlotMapping = staticCodeObject.StaticLexPad.SlotMapping;
        this.LexPad.Storage = (RakudoObject[])staticCodeObject.StaticLexPad.Storage.clone();

        // Set outer context.
        if (staticCodeObject.OuterForNextInvocation != null)
        {
            this.Outer = staticCodeObject.OuterForNextInvocation;
        }
        else if (staticCodeObject.OuterBlock.CurrentContext != null)
        {
            this.Outer = staticCodeObject.OuterBlock.CurrentContext;
        }
        else
        {
            // Auto-close. In this we go setting up fake contexts
            // that use the static lexpad until we find a real one.
            Context curContext = this;
            RakudoCodeRef.Instance outerBlock = staticCodeObject.OuterBlock;
            while (outerBlock != null)
            {
                // If we found a block with a context, we're done.
                if (outerBlock.CurrentContext != null)
                {
                    curContext.Outer = outerBlock.CurrentContext;
                    break;
                }

                // Build the fake context.
                Context outerContext = new Context();
                outerContext.StaticCodeObject = outerBlock;
                outerContext.LexPad = outerBlock.StaticLexPad;

                // Link it.
                curContext.Outer = outerContext;

                // Step back one level.
                curContext = outerContext;
                outerBlock = outerBlock.OuterBlock;
            }
        }
    }
}


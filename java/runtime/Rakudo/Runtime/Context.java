package Rakudo.Runtime;

import java.util.*;       // HashMap
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
    /// Lexpad. Note that we'll in the end have something much smarter
    /// for this but it'll do for now.
    /// </summary>
    public HashMap<String, RakudoObject> LexPad;

    /// <summary>
    /// The capture passed as part of the current call.
    /// </summary>
    public RakudoObject Capture;

    /// <summary>
    /// Creates an empty, uninitialized context.
    /// </summary>
    public Context()
    {
    }

    /// <summary>
    /// Initializes the context.
    /// </summary>
    /// <param name="StaticCodeObject"></param>
    /// <param name="Caller"></param>
    public Context(RakudoCodeRef.Instance StaticCodeObject, Context Caller, RakudoObject Capture)
    {
        // Set up static code object and caller pointers.
        this.StaticCodeObject = StaticCodeObject;
        this.Caller = Caller;
        this.Capture = Capture;

        // Static sub object should have this as the current
        // context.
//      StaticCodeObject.CurrentContext = this;

        // Lex pad should be copy of the static one.
        // XXX This isn't quite what we want in the long run, but it
        // does fine for now.
//      this.LexPad = new HashMap<String, RakudoObject>(StaticCodeObject.StaticLexPad);

        // Set outer context.
        RakudoCodeRef.Instance OuterBlock = StaticCodeObject.OuterBlock;
//      if (OuterBlock.CurrentContext != null)
//      {
//          this.Outer = OuterBlock.CurrentContext;
//      }
//      else
        {
            // Auto-close. In this we go setting up fake contexts
            // that use the static lexpad until we find a real one.
            Context CurContext = this;
            while (OuterBlock != null)
            {
                // If we found a block with a context, we're done.
//              if (OuterBlock.CurrentContext != null)
//              {
//                  CurContext.Outer = OuterBlock.CurrentContext;
//                  break;
//              }

                // Build the fake context.
                Context OuterContext = new Context();
                OuterContext.StaticCodeObject = OuterBlock;
//              OuterContext.LexPad = OuterBlock.StaticLexPad;

                // Link it.
                CurContext.Outer = OuterContext;
                
                // Step back one level.
                CurContext = OuterContext;
                OuterBlock = OuterBlock.OuterBlock;
            }
        }
    }
}


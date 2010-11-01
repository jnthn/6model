package Rakudo.Runtime.Exceptions;

import Rakudo.Metamodel.RakudoObject;

/// <summary>
/// Represents an exception handler.
/// </summary>
public class Handler  // struct in the C# version
{
    /// <summary>
    /// The type of exception that the handler accepts.
    /// </summary>
    public int Type;

    /// <summary>
    /// Something invokable that will handle the exception.
    /// </summary>
    public RakudoObject HandleBlock;

    /// <summary>
    /// Set up a handler of the given type and with the given block as
    /// the handler to run.
    /// </summary>
    /// <param name="Type"></param>
    /// <param name="HandleBlock"></param>
    public Handler(int type, RakudoObject handleBlock)
    {
        this.Type = type;
        this.HandleBlock = handleBlock;
    }
}


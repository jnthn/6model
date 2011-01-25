package Rakudo.Runtime.Exceptions;

import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representations.RakudoCodeRef;





/// <summary>
/// This exception is thrown to actually unwind the (JVM) stack after
/// we run an exception handler.
/// </summary>
public class LeaveStackUnwinderException extends RuntimeException // not Exception, see http://www.javapractices.com/topic/TopicAction.do?Id=129
{
    /// <summary>
    /// The block we're looking for.
    /// </summary>
    public RakudoCodeRef.Instance TargetBlock;

    /// <summary>
    /// The value to exit with.
    /// </summary>
    public RakudoObject PayLoad;

    /// <summary>
    /// Creates a LeaveStackUnwinderException to target the given block
    /// and exit it with the specified payload.
    /// </summary>
    /// <param name="TargetBlock"></param>
    /// <param name="PayLoad"></param>
    public LeaveStackUnwinderException(RakudoCodeRef.Instance targetBlock, RakudoObject payLoad)
    {
        this.TargetBlock = targetBlock;
        this.PayLoad = payLoad;
    }
}


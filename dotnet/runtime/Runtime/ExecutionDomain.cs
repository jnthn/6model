using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;

namespace Rakudo.Runtime
{
    /// <summary>
    /// An execution domain is the root of all state we keep around for a
    /// running Perl 6 program. We may manage to have multiple of these in
    /// memory, and they'd be isolated from each other.
    /// </summary>
    public class ExecutionDomain
    {
        public RakudoObject DefaultStrBoxType;
    }
}

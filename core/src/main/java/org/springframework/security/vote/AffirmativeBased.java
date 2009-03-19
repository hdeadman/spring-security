/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.vote;

import java.util.List;

import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttribute;


/**
 * Simple concrete implementation of  {@link org.springframework.security.AccessDecisionManager} that grants access if any
 * <code>AccessDecisionVoter</code> returns an affirmative response.
 */
public class AffirmativeBased extends AbstractAccessDecisionManager {
    //~ Methods ========================================================================================================

    /**
     * This concrete implementation simply polls all configured  {@link AccessDecisionVoter}s and grants access
     * if any <code>AccessDecisionVoter</code> voted affirmatively. Denies access only if there was a deny vote AND no
     * affirmative votes.<p>If every <code>AccessDecisionVoter</code> abstained from voting, the decision will
     * be based on the {@link #isAllowIfAllAbstainDecisions()} property (defaults to false).</p>
     *
     * @param authentication the caller invoking the method
     * @param object the secured object
     * @param configAttributes the configuration attributes associated with the method being invoked
     *
     * @throws AccessDeniedException if access is denied
     */
    public void decide(Authentication authentication, Object object, List<ConfigAttribute> configAttributes)
            throws AccessDeniedException {
        int deny = 0;

        for (AccessDecisionVoter voter : getDecisionVoters()) {
            int result = voter.vote(authentication, object, configAttributes);

            if (logger.isDebugEnabled()) {
                logger.debug("Voter: " + voter + ", returned: " + result);
            }

            switch (result) {
            case AccessDecisionVoter.ACCESS_GRANTED:
                return;

            case AccessDecisionVoter.ACCESS_DENIED:
                deny++;

                break;

            default:
                break;
            }
        }

        if (deny > 0) {
            throw new AccessDeniedException(messages.getMessage("AbstractAccessDecisionManager.accessDenied",
                    "Access is denied"));
        }

        // To get this far, every AccessDecisionVoter abstained
        checkAllowIfAllAbstainDecisions();
    }
}

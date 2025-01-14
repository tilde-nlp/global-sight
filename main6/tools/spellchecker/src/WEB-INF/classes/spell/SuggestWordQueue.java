/**
 * Copyright 2002-2004 The Apache Software Foundation
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
package spell;

import org.apache.lucene.util.PriorityQueue;

/**
 * Priority queue to sort SuggestWord objects.
 *
 * @author Nicolas Maisonneuve
 */
final class SuggestWordQueue
    extends PriorityQueue
{
    SuggestWordQueue (int size)
    {
        initialize(size);
    }

    protected final boolean lessThan (Object a, Object b)
    {
        SuggestWord wa = (SuggestWord) a;
        SuggestWord wb = (SuggestWord) b;
        int val = wa.compareTo(wb);
        return val < 0;
    }
}

/*
 *  Copyright 2021 ChenJun (power4j@outlook.com & https://github.com/John-Chan)
 *
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.gnu.org/licenses/lgpl.html
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.power4j.fist.data.tree

import cn.hutool.core.lang.tree.Tree
import cn.hutool.core.lang.tree.TreeNodeConfig
import cn.hutool.core.util.RandomUtil
import cn.hutool.json.JSONUtil
import com.power4j.fist.data.tree.domain.OrgNodeIdx
import org.tools4j.groovytables.GroovyTables
import spock.lang.Specification

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/9/27
 * @since 1.0
 */
class NodeIdxTreeBuilderTest extends Specification {

	//
	//        (0)
	//       /   \
	//     (1)   (2)
	//    /  \
	// (101) (102)
	//
	List<OrgNodeIdx> links = GroovyTables.createListOf(OrgNodeIdx.class).fromTable {
		ancestor   | descendant    | distance
		0L         | 0L            | 0
		1L         | 1L            | 0
		2L         | 2L            | 0
		101L       | 101L          | 0
		102L       | 102L          | 0
		0L         | 1L            | 1
		0L         | 2L            | 1
		1L         | 101L          | 1
		1L         | 102L          | 1
		0L         | 101L          | 2
		0L         | 102L          | 2
	}

	TreeCustomizer<Long> customizer = new TreeCustomizer<Long>(){

		void customize(Tree<Long> tree) {
			tree.setName(String.format("ORG-%04d",tree.getId()));
			tree.setWeight(tree.getId());
			tree.putExtra("hot", RandomUtil.randomInt())
		}

		@Override
		void customize(Collection<? extends Tree<Long>> elements) {
			for(Tree<Long> tree : elements){
				customize(tree)
			}
		}
	}

	TreeNodeConfig config = TreeNodeConfig.DEFAULT_CONFIG.setWeightKey("order")

    def "Test Make Tree"() {
		given:
		TreeBuilder<Long> maker = new TreeBuilder().nodeConfig(config).customizer(customizer).nodes(links);

		when:
		Tree<Long> root = maker.build(0L)

		then:
		root != null
		System.out.println(JSONUtil.toJsonPrettyStr(root))

		then:
		root.getId() == 0

		then:
		List<Tree<Long>> lv1 = root.getChildren()
		Tree<Long> org1 = lv1.get(0)

		org1.getWeight() == 1
		org1.getId() == 1

		Tree<Long> org2 = lv1.get(1)
		org2.getWeight() == 2
		org2.getId() == 2

		then:
		List<Tree<Long>> lv2 = org1.getChildren()
		Tree<Long> org101 = lv2.get(0)

		org101.getWeight() == 101
		org101.getId() == 101

		Tree<Long> org102 = lv2.get(1)
		org102.getWeight() == 102
		org102.getId() == 102
    }
}
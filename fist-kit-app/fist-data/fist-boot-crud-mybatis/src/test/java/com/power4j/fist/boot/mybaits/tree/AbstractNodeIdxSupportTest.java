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

package com.power4j.fist.boot.mybaits.tree;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2022/1/24
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureTestDatabase
class AbstractNodeIdxSupportTest {

	@Autowired
	private OrgIdxMapper orgIdxMapper;

	@Autowired
	private OrgTreeService orgTreeService;

	void makeSingleRoot() {
		// @formatter:off

		//
		//        (0)
		//        / \
		//     (10) (20)
		//     /      \
		// (101)     (201)
		//          /   \
		//      (2011) (2012)
		//

		// @formatter:on

		orgTreeService.generatePath(0L, null);
		orgTreeService.generatePath(10L, 0L);
		orgTreeService.generatePath(20L, 0L);
		orgTreeService.generatePath(101L, 10L);
		orgTreeService.generatePath(201L, 20L);
		orgTreeService.generatePath(2011L, 201L);
		orgTreeService.generatePath(2012L, 201L);
	}

	void makeMultiRoot() {
		// @formatter:off

		//     (10)    (20)     (30)
		//      /       /
		//  (101)     (201)
		//           /   \
		//      (2011)   (2012)
		//

		// @formatter:on

		orgTreeService.generatePath(10L, null);
		orgTreeService.generatePath(20L, null);
		orgTreeService.generatePath(30L, null);
		orgTreeService.generatePath(101L, 10L);
		orgTreeService.generatePath(201L, 20L);
		orgTreeService.generatePath(2011L, 201L);
		orgTreeService.generatePath(2012L, 201L);
	}

	@AfterEach
	void tearDown() {
		orgTreeService.getRepository().deleteAll();
	}

	@Test
	void getAll() {
		makeSingleRoot();
		List<OrgIdx> list = orgTreeService.getAll(0, 0);
		Assertions.assertEquals(7, list.size());
	}

	@Test
	void subTreeNodes() {
		makeSingleRoot();
		List<Long> idList1 = Arrays.asList(101L, 201L);
		Set<Long> sub1 = orgTreeService.subTreeNodes(idList1);
		Set<Long> expected1 = new HashSet<>(Arrays.asList(101L, 201L, 2011L, 2012L));
		Assertions.assertEquals(expected1, sub1);

		List<Long> idList2 = Arrays.asList(20L, 201L);
		Set<Long> sub2 = orgTreeService.subTreeNodes(idList2);
		Set<Long> expected2 = new HashSet<>(Arrays.asList(20L, 201L, 2011L, 2012L));
		Assertions.assertEquals(expected2, sub2);
	}

	@Test
	void movingNonLeafNodeShouldRebuildDescendantPaths() {
		makeSingleRoot();

		orgTreeService.moveSubtree(20L, 10L);

		Set<String> paths = orgTreeService.findAllDescendant(10L, 0, null)
			.stream()
			.map(path -> path.getAncestor() + ">" + path.getDescendant() + ":" + path.getDistance())
			.collect(Collectors.toSet());
		Set<String> expected = new HashSet<>(
				Arrays.asList("10>10:0", "10>101:1", "10>20:1", "10>201:2", "10>2011:3", "10>2012:3"));

		Assertions.assertEquals(expected, paths);
		Assertions.assertTrue(orgTreeService.findAllDescendant(0L, 0, null)
			.stream()
			.noneMatch(path -> (path.getDescendant().equals(201L) && path.getDistance() == 2)
					|| (Set.of(2011L, 2012L).contains(path.getDescendant()) && path.getDistance() == 3)));
	}

	@Test
	void findSharedRoot() {
		makeMultiRoot();
		List<Long> idList1 = Arrays.asList(101L, 2011L);
		Set<Long> roots1 = orgTreeService.findSharedRoot(idList1);
		Set<Long> expected1 = new HashSet<>(Arrays.asList(10L, 20L));
		Assertions.assertEquals(expected1, roots1);

		List<Long> idList2 = Arrays.asList(20L, 2012L);
		Set<Long> roots21 = orgTreeService.findSharedRoot(idList2);
		Set<Long> expected21 = new HashSet<>(Collections.singletonList(20L));
		Assertions.assertEquals(expected21, roots21);

		List<Long> idList3 = Arrays.asList(201L, 2011L, 2012L);
		Set<Long> roots3 = orgTreeService.findSharedRoot(idList3);
		Set<Long> expected3 = new HashSet<>(Collections.singletonList(20L));
		Assertions.assertEquals(expected3, roots3);
	}

}
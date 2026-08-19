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
import org.junit.jupiter.api.BeforeEach;
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

	@BeforeEach
	void setUp() {
		orgTreeService.getRepository().deleteAll();
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
	void movingSubtreeToGrandparentShouldKeepAllDescendantPaths() {
		// 根节点（0）下有 XD（10），XD 下有 T1（20），T1 下有 T1-1（21）。
		// T1 下再创建 T2（30）及 T2-1（31），然后将 T2 移动到 XD 下。
		orgTreeService.generatePath(0L, null);
		orgTreeService.generatePath(10L, 0L);
		orgTreeService.generatePath(20L, 10L);
		orgTreeService.generatePath(21L, 20L);
		orgTreeService.generatePath(30L, 20L);
		orgTreeService.generatePath(31L, 30L);

		orgTreeService.moveSubtree(30L, 10L);

		Set<String> paths = orgTreeService.findAllDescendant(10L, 0, null)
			.stream()
			.map(path -> path.getAncestor() + ">" + path.getDescendant() + ":" + path.getDistance())
			.collect(Collectors.toSet());
		Set<String> expected = new HashSet<>(Arrays.asList("10>10:0", "10>20:1", "10>21:2", "10>30:1", "10>31:2"));

		Assertions.assertEquals(expected, paths);
		Assertions.assertEquals(new HashSet<>(Arrays.asList(10L, 20L, 21L, 30L, 31L)),
				orgTreeService.subTreeNodes(Collections.singleton(10L)));
		Assertions.assertEquals(new HashSet<>(Arrays.asList(20L, 21L)),
				orgTreeService.subTreeNodes(Collections.singleton(20L)));
	}

	@Test
	void generatePathAfterMovingSubtreeShouldUseNewAncestors() {
		orgTreeService.generatePath(0L, null);
		orgTreeService.generatePath(10L, 0L);
		orgTreeService.generatePath(20L, 10L);
		orgTreeService.generatePath(30L, 20L);
		orgTreeService.moveSubtree(30L, 10L);

		orgTreeService.generatePath(31L, 30L);

		Assertions.assertTrue(orgTreeService.findAllDescendant(10L, 0, null)
			.stream()
			.anyMatch(path -> path.getDescendant().equals(31L) && path.getDistance() == 2));
		Assertions.assertTrue(orgTreeService.findAllDescendant(20L, 0, null)
			.stream()
			.noneMatch(path -> path.getDescendant().equals(31L)));
	}

	@Test
	void deletingSubtreePathsBeforeRecreatingSameIdsShouldBuildNewPaths() {
		orgTreeService.generatePath(0L, null);
		orgTreeService.generatePath(10L, 0L);
		orgTreeService.generatePath(20L, 10L);
		orgTreeService.generatePath(30L, 20L);
		orgTreeService.generatePath(31L, 30L);

		// 按叶子到根的顺序清理关系，模拟部门同步的级联删除。
		orgTreeService.removeAllPath(31L);
		orgTreeService.removeAllPath(30L);
		orgTreeService.generatePath(30L, 10L);
		orgTreeService.generatePath(31L, 30L);

		Set<String> paths = orgTreeService.findAllDescendant(10L, 0, null)
			.stream()
			.map(path -> path.getAncestor() + ">" + path.getDescendant() + ":" + path.getDistance())
			.collect(Collectors.toSet());
		Set<String> expected = new HashSet<>(Arrays.asList("10>10:0", "10>20:1", "10>30:1", "10>31:2"));

		Assertions.assertEquals(expected, paths);
		Assertions.assertTrue(orgTreeService.findAllDescendant(20L, 0, null)
			.stream()
			.noneMatch(path -> Set.of(30L, 31L).contains(path.getDescendant())));
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

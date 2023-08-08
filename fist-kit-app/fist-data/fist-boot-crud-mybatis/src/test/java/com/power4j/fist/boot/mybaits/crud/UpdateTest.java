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

package com.power4j.fist.boot.mybaits.crud;

import com.power4j.fist.boot.mybaits.crud.repository.People;
import com.power4j.fist.boot.mybaits.crud.repository.PeopleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2022/1/27
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureTestDatabase
class UpdateTest {

	@Autowired
	private PeopleRepository peopleRepository;

	@Test
	void saveOne() {
		People people = People.of(-100L);
		peopleRepository.saveOne(people);
		People people2 = peopleRepository.findOneById(-100L).orElse(null);
		Assertions.assertNotNull(people2);
	}

	@Test
	void deleteAllById() {
		List<Long> ids = Arrays.asList(1L, 2L, 3L, 4L, 5L);
		List<People> peoples = TestUtils.createPeopleEntity(ids);
		peopleRepository.saveAll(peoples);

		peopleRepository.deleteAllById(ids);
		long count2 = peopleRepository.findAllById(ids).size();
		Assertions.assertEquals(0L, count2);
	}

}

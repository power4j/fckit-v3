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

import com.power4j.fist.boot.mybaits.crud.repository.Book;
import com.power4j.fist.boot.mybaits.crud.repository.People;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2022/1/27
 * @since 1.0
 */
@UtilityClass
public class TestUtils {

	List<Book> createBookEntity(long idStart, int size) {
		List<Long> idList = LongStream.range(idStart, idStart + size).boxed().collect(Collectors.toList());
		return createBookEntity(idList);
	}

	List<Book> createBookEntity(Collection<Long> ids) {
		return ids.stream().map(Book::of).collect(Collectors.toList());
	}

	List<People> createPeopleEntity(Collection<Long> ids) {
		return ids.stream().map(People::of).collect(Collectors.toList());
	}

}

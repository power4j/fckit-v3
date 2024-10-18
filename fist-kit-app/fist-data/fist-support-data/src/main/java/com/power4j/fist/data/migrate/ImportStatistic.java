/*
 * Copyright 2024. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.power4j.fist.data.migrate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Getter
@Setter
@Builder
public class ImportStatistic {

	private long receiveCount;

	private long skipCount;

	private long insertCount;

	private long deleteCount;

	private long updateCount;

	public static ImportStatistic of() {
		return ImportStatistic.builder().build();
	}

	public ImportStatistic merge(ImportStatistic other) {
		this.receiveCount += other.getReceiveCount();
		this.skipCount += other.getSkipCount();
		this.insertCount += other.getInsertCount();
		this.deleteCount += other.getDeleteCount();
		this.updateCount += other.getUpdateCount();
		return this;
	}

	public void addSkipCount(int val) {
		this.skipCount += val;
	}

	public void addInsertCount(int val) {
		this.insertCount += val;
	}

	public void addDeleteCount(int val) {
		this.deleteCount += val;
	}

	public void addUpdateCount(int val) {
		this.updateCount += val;
	}

	public void addReceiveCount(int val) {
		this.receiveCount += val;
	}

	public void reset() {
		this.receiveCount = 0;
		this.skipCount = 0;
		this.insertCount = 0;
		this.deleteCount = 0;
		this.updateCount = 0;
	}

}

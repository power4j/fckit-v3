/*
 * Copyright 2021 ChenJun (power4j@outlook.com & https://github.com/John-Chan)
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

package com.power4j.fist.data.tree.domain;

import lombok.Data;
import org.springframework.lang.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2022/5/7
 * @since 2022.0.3
 * @param <ID> ID 类型
 */
@Data
public class TreeNode<ID> implements Node<ID, TreeNode<ID>>, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private ID id;

	@Nullable
	private ID parentId;

	@Nullable
	private List<TreeNode<ID>> children;

	public static <ID> TreeNode<ID> of(ID id, @Nullable ID parentId) {
		TreeNode<ID> node = new TreeNode<>();
		node.setId(id);
		node.setParentId(parentId);
		return node;
	}

	@Override
	public void appendChild(TreeNode<ID> child) {
		if (Objects.isNull(children)) {
			children = new ArrayList<>(2);
		}
		children.add(child);
	}

}

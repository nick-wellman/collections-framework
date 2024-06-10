package com.nickwellman.collections.repository;

import java.sql.SQLException;

public interface MutableRepository extends Repository {

    /**
     * Creates a new {@link MutableRepositoryItem} but does not save it to the database
     *
     * @param itemDescriptor The item descriptor to create the new item in
     * @return A {@link MutableRepositoryItem} that was created
     */
    MutableRepositoryItem createItem(String itemDescriptor);

    /**
     * Adds a {@link RepositoryItem} to the database
     *
     * @param mutableRepositoryItem The item to add
     * @return The {@link RepositoryItem} that was added
     * @throws IllegalStateException Throws {@link IllegalStateException} if the item with the id already exists
     */
    RepositoryItem addItem(MutableRepositoryItem mutableRepositoryItem) throws IllegalStateException, SQLException;

    /**
     * Updates the database with the passed {@link RepositoryItem}.  This is a full in-place update, no merging will be attempted
     *
     * @param repositoryItem The {@link RepositoryItem} to update
     * @return The {@link RepositoryItem} that was updated
     */
    RepositoryItem updateItem(RepositoryItem repositoryItem, String... fieldsToUpdate);
}

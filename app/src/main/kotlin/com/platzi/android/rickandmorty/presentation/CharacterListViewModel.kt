package com.platzi.android.rickandmorty.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.platzi.android.rickandmorty.api.*
import com.platzi.android.rickandmorty.ui.CharacterListFragment
import com.platzi.android.rickandmorty.utils.showLongToast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_character_list.*

class CharacterListViewModel(
    private val characterRequest: CharacterRequest
) : ViewModel() {

    private val disposable = CompositeDisposable()

    private val _events = MutableLiveData<Event<CharacterListNavigation>>()

    val events: LiveData<Event<CharacterListNavigation>>get() = _events

    private var currentPage = 1
    private var isLastPage = false
    private var isLoading = false

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    fun onLoadMoreItems(visibleItemCount: Int, firstVisibleItemPosition: Int, totalItemCount: Int) {
        if (isLoading || isLastPage || !isInFooter(visibleItemCount, firstVisibleItemPosition, totalItemCount)) {
            return
        }

        currentPage += 1
        onGetAllCharacters()
    }

    private fun isInFooter(
        visibleItemCount: Int,
        firstVisibleItemPosition: Int,
        totalItemCount: Int
    ): Boolean {
        return visibleItemCount + firstVisibleItemPosition >= totalItemCount
                && firstVisibleItemPosition >= 0
                && totalItemCount >= PAGE_SIZE
    }

    fun onRetryGetAllCharacter(itemCount: Int) {
        if (itemCount > 0) {
            //srwCharacterList.isRefreshing = false
            _events.value = Event(CharacterListNavigation.HideLoading)
            return
        }

        onGetAllCharacters()
    }

    fun onGetAllCharacters(){
        disposable.add(
            characterRequest
                .getService<CharacterService>()
                .getAllCharacters(currentPage)
                .map(CharacterResponseServer::toCharacterServerList)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    //srwCharacterList.isRefreshing = true
                    _events.value = Event(CharacterListNavigation.ShowLading)
                }
                .subscribe({ characterList ->
                    if (characterList.size < PAGE_SIZE) {
                        isLastPage = true
                    }

                    //srwCharacterList.isRefreshing = false
                    //characterGridAdapter.addData(characterList)
                    _events.value = Event(CharacterListNavigation.HideLoading)
                    _events.value = Event(CharacterListNavigation.ShowCharacterList(characterList))

                }, { error ->
                    isLastPage = true
                    //srwCharacterList.isRefreshing = false
                    //context?.showLongToast("Error -> ${error.message}")
                    _events.value = Event(CharacterListNavigation.HideLoading)
                    _events.value = Event(CharacterListNavigation.ShowCharacterError(error))
                })
        )
    }

    companion object{
        private const val PAGE_SIZE = 20
    }

    sealed class CharacterListNavigation {
        data class ShowCharacterError(val error: Throwable) : CharacterListNavigation()
        data class ShowCharacterList(val characterList: List<CharacterServer>) : CharacterListNavigation()
        object HideLoading : CharacterListNavigation()
        object ShowLading : CharacterListNavigation()
    }
}